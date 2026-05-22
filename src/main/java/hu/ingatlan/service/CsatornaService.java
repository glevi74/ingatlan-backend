package hu.ingatlan.service;

import hu.ingatlan.domain.Csatorna;
import hu.ingatlan.domain.CsatornaTags;
import hu.ingatlan.domain.Felhasznalo;
import hu.ingatlan.domain.Uzenet;
import hu.ingatlan.dto.CsatornaDto;
import hu.ingatlan.repository.CsatornaRepository;
import hu.ingatlan.repository.CsatornatagsRepository;
import hu.ingatlan.repository.FelhasznaloRepository;
import hu.ingatlan.repository.UzenetRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CsatornaService {

    @Inject CsatornaRepository csatornaRepo;
    @Inject CsatornatagsRepository tagsRepo;
    @Inject UzenetRepository uzenetRepo;
    @Inject FelhasznaloRepository felhasznaloRepo;
    @Inject IrodaContext ctx;

    // ── Csatorna lista ─────────────────────────────────────────────────────

    /**
     * Az aktuális felhasználó számára látható csatornák.
     * Mellékhatás: ha még nincs ALTALANOS csatorna az irodában, automatikusan létrehozza.
     */
    @Transactional
    public List<CsatornaDto.Response> listAll() {
        UUID irodaId = ctx.irodaId();
        if (irodaId == null) return List.of();   // ADMIN iroda-kontextus nélkül → üres

        // Garantálja, hogy létezik az általános csatorna
        ensureAltalanos(irodaId);

        List<Csatorna> csatornak;
        if (ctx.isAdmin()) {
            // Admin: csak ALTALANOS + KAPCSOLT látható
            csatornak = csatornaRepo.listByIrodaAndTipusok(irodaId,
                    List.of(Csatorna.CsatornaTipus.ALTALANOS, Csatorna.CsatornaTipus.KAPCSOLT));
        } else {
            UUID me = ctx.felhasznaloId();
            // Közös csatornák (ALTALANOS + KAPCSOLT)
            List<Csatorna> nyilvan = csatornaRepo.listByIrodaAndTipusok(irodaId,
                    List.of(Csatorna.CsatornaTipus.ALTALANOS, Csatorna.CsatornaTipus.KAPCSOLT));
            // PRIVAT + CSOPORT, ahol én tag vagyok
            List<UUID> myChannelIds = tagsRepo.findByFelhasznalo(me).stream()
                    .map(t -> t.csatornaId)
                    .collect(Collectors.toList());
            List<Csatorna> sajat = myChannelIds.isEmpty()
                    ? List.of()
                    : csatornaRepo.listByIds(myChannelIds);
            List<Csatorna> all = new ArrayList<>(nyilvan);
            all.addAll(sajat);
            csatornak = all;
        }

        return csatornak.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Csatorna létrehozás ────────────────────────────────────────────────

    @Transactional
    public CsatornaDto.Response create(CsatornaDto.CreateRequest dto) {
        if (ctx.isAdmin()) {
            throw new ForbiddenException("Admin nem hozhat létre privát/csoport csatornát.");
        }
        UUID irodaId = ctx.irodaIdOrThrow();
        UUID me = ctx.felhasznaloId();

        if (dto.tipus == Csatorna.CsatornaTipus.PRIVAT || dto.tipus == Csatorna.CsatornaTipus.CSOPORT) {
            if (dto.tagIds == null || dto.tagIds.isEmpty()) {
                throw new BadRequestException("PRIVAT/CSOPORT csatornához tagok szükségesek.");
            }
            if (dto.tipus == Csatorna.CsatornaTipus.PRIVAT && dto.tagIds.size() != 1) {
                throw new BadRequestException("Privát csatornához pontosan 1 tag szükséges.");
            }
            // PRIVAT: ne duplikáljuk, ha már van ilyen csatorna a két felhasználó között
            if (dto.tipus == Csatorna.CsatornaTipus.PRIVAT) {
                UUID other = dto.tagIds.get(0);
                Csatorna existing = findExistingPrivat(irodaId, me, other);
                if (existing != null) return toResponse(existing);
            }
        } else if (dto.tipus == Csatorna.CsatornaTipus.ALTALANOS) {
            throw new BadRequestException("ALTALANOS csatorna csak automatikusan hozható létre.");
        }

        Csatorna c = new Csatorna();
        c.nev = dto.nev;
        c.tipus = dto.tipus;
        c.irodaId = irodaId;
        c.letrehozoId = me;
        c.ingatlanId = dto.ingatlanId;
        c.feladatId = dto.feladatId;
        c.ugyfelId = dto.ugyfelId;
        c.megbizasId = dto.megbizasId;
        csatornaRepo.persist(c);

        // Tagok hozzáadása PRIVAT/CSOPORT esetén (creator + megadott tagok)
        if (dto.tipus == Csatorna.CsatornaTipus.PRIVAT || dto.tipus == Csatorna.CsatornaTipus.CSOPORT) {
            addTag(c.id, me);
            for (UUID tagId : dto.tagIds) {
                if (!tagId.equals(me)) addTag(c.id, tagId);
            }
        }

        return toResponse(c);
    }

    // ── Kapcsolt csatorna get-or-create ───────────────────────────────────

    @Transactional
    public CsatornaDto.Response getOrCreateKapcsolt(UUID ingatlanId, UUID feladatId,
                                                     UUID ugyfelId, UUID megbizasId) {
        UUID irodaId = ctx.irodaId();
        if (irodaId == null) {
            throw new BadRequestException("Kapcsolt csatornához iroda-kontextus szükséges.");
        }

        Csatorna c = csatornaRepo.findKapcsolt(irodaId, ingatlanId, feladatId, ugyfelId, megbizasId)
                .orElseGet(() -> {
                    Csatorna nc = new Csatorna();
                    nc.tipus = Csatorna.CsatornaTipus.KAPCSOLT;
                    nc.irodaId = irodaId;
                    nc.letrehozoId = ctx.isAdmin() ? null : ctx.felhasznaloId();
                    nc.ingatlanId = ingatlanId;
                    nc.feladatId = feladatId;
                    nc.ugyfelId = ugyfelId;
                    nc.megbizasId = megbizasId;
                    csatornaRepo.persist(nc);
                    return nc;
                });

        return toResponse(c);
    }

    // ── Üzenetek ──────────────────────────────────────────────────────────

    public List<CsatornaDto.UzenetResponse> getUzenetek(UUID csatornaId, LocalDateTime since) {
        Csatorna c = getAccessibleCsatorna(csatornaId);
        List<Uzenet> uzenetek = since != null
                ? uzenetRepo.findByCsatornaSince(csatornaId, since)
                : uzenetRepo.findLastByCsatorna(csatornaId, 200);
        return uzenetek.stream().map(this::toUzenetResponse).collect(Collectors.toList());
    }

    @Transactional
    public CsatornaDto.UzenetResponse sendUzenet(UUID csatornaId, CsatornaDto.UzenetRequest dto) {
        Csatorna c = getAccessibleCsatorna(csatornaId);

        boolean vanSzoveg = dto.szoveg != null && !dto.szoveg.isBlank();
        boolean vanKapcsolt = dto.kapcsoltId != null;
        if (!vanSzoveg && !vanKapcsolt) {
            throw new BadRequestException("Az üzenetnek szöveget vagy hivatkozott elemet kell tartalmaznia.");
        }

        Uzenet u = new Uzenet();
        u.csatornaId = csatornaId;
        u.feladoId = ctx.felhasznaloId();
        u.szoveg = vanSzoveg ? dto.szoveg.strip() : null;
        u.irodaId = c.irodaId;
        u.kapcsoltTipus = dto.kapcsoltTipus;
        u.kapcsoltId    = dto.kapcsoltId;
        u.kapcsoltNev   = dto.kapcsoltNev;
        uzenetRepo.persist(u);

        return toUzenetResponse(u);
    }

    // ── Belső segédmetódusok ───────────────────────────────────────────────

    private void ensureAltalanos(UUID irodaId) {
        csatornaRepo.findAltalanos(irodaId).orElseGet(() -> {
            Csatorna c = new Csatorna();
            c.nev = "Általános";
            c.tipus = Csatorna.CsatornaTipus.ALTALANOS;
            c.irodaId = irodaId;
            csatornaRepo.persist(c);
            return c;
        });
    }

    private void addTag(UUID csatornaId, UUID felhasznaloId) {
        if (!tagsRepo.isTags(csatornaId, felhasznaloId)) {
            CsatornaTags tag = new CsatornaTags();
            tag.csatornaId = csatornaId;
            tag.felhasznaloId = felhasznaloId;
            tagsRepo.persist(tag);
        }
    }

    /** Visszaadja a két felhasználó közötti meglévő PRIVAT csatornát, ha van. */
    private Csatorna findExistingPrivat(UUID irodaId, UUID userA, UUID userB) {
        // PRIVAT csatornák ahol A tag
        List<UUID> aChannels = tagsRepo.findByFelhasznalo(userA).stream()
                .map(t -> t.csatornaId).collect(Collectors.toList());
        if (aChannels.isEmpty()) return null;

        return csatornaRepo.listByIrodaAndTipusok(irodaId,
                        List.of(Csatorna.CsatornaTipus.PRIVAT)).stream()
                .filter(c -> aChannels.contains(c.id))
                .filter(c -> tagsRepo.isTags(c.id, userB))
                .findFirst().orElse(null);
    }

    private Csatorna getAccessibleCsatorna(UUID csatornaId) {
        Csatorna c = csatornaRepo.findById(csatornaId);
        if (c == null) throw new NotFoundException("Csatorna nem található: " + csatornaId);

        UUID irodaId = ctx.irodaId();
        if (irodaId != null && !irodaId.equals(c.irodaId)) {
            throw new NotFoundException("Csatorna nem található: " + csatornaId);
        }

        // Admin nem fér hozzá PRIVAT/CSOPORT csatornához
        if (ctx.isAdmin() &&
                (c.tipus == Csatorna.CsatornaTipus.PRIVAT || c.tipus == Csatorna.CsatornaTipus.CSOPORT)) {
            throw new ForbiddenException("Admin nem fér hozzá privát/csoport csatornákhoz.");
        }

        // Nem-Admin: PRIVAT/CSOPORT esetén tagnak kell lennie
        if (!ctx.isAdmin() &&
                (c.tipus == Csatorna.CsatornaTipus.PRIVAT || c.tipus == Csatorna.CsatornaTipus.CSOPORT)) {
            if (!tagsRepo.isTags(csatornaId, ctx.felhasznaloId())) {
                throw new ForbiddenException("Nincs hozzáférése ehhez a csatornához.");
            }
        }

        return c;
    }

    // ── Leképezők ──────────────────────────────────────────────────────────

    private CsatornaDto.Response toResponse(Csatorna c) {
        CsatornaDto.Response r = new CsatornaDto.Response();
        r.setId(c.id);
        r.setNev(c.nev);
        r.setTipus(c.tipus);
        r.setIrodaId(c.irodaId);
        r.setLetrehozoId(c.letrehozoId);
        r.setLetrehozva(c.letrehozva);
        r.setIngatlanId(c.ingatlanId);
        r.setFeladatId(c.feladatId);
        r.setUgyfelId(c.ugyfelId);
        r.setMegbizasId(c.megbizasId);
        r.setUzenetSzam(uzenetRepo.countByCsatorna(c.id));

        // Utolsó üzenet előnézet
        List<Uzenet> lastOne = uzenetRepo.findLastByCsatorna(c.id, 1);
        if (!lastOne.isEmpty()) {
            Uzenet last = lastOne.get(0);
            String preview = last.szoveg != null && !last.szoveg.isBlank()
                    ? (last.szoveg.length() > 60 ? last.szoveg.substring(0, 60) + "…" : last.szoveg)
                    : (last.kapcsoltNev != null ? "📎 " + last.kapcsoltNev : "");
            r.setUtolsoUzenetSzoveg(preview);
            r.setUtolsoUzenetIdeje(last.letrehozva);
        }

        // Tagok betöltése PRIVAT/CSOPORT esetén
        if (c.tipus == Csatorna.CsatornaTipus.PRIVAT || c.tipus == Csatorna.CsatornaTipus.CSOPORT) {
            r.setTagok(tagsRepo.findByCsatorna(c.id).stream().map(tag -> {
                CsatornaDto.TagResponse tr = new CsatornaDto.TagResponse();
                tr.setId(tag.felhasznaloId);
                Felhasznalo f = felhasznaloRepo.findById(tag.felhasznaloId);
                tr.setNev(f != null ? (f.nev != null ? f.nev : f.felhasznalonev) : "?");
                return tr;
            }).collect(Collectors.toList()));
        }

        return r;
    }

    private CsatornaDto.UzenetResponse toUzenetResponse(Uzenet u) {
        CsatornaDto.UzenetResponse r = new CsatornaDto.UzenetResponse();
        r.setId(u.id);
        r.setCsatornaId(u.csatornaId);
        r.setFeladoId(u.feladoId);
        r.setSzoveg(u.szoveg);
        r.setLetrehozva(u.letrehozva);
        r.setKapcsoltTipus(u.kapcsoltTipus);
        r.setKapcsoltId(u.kapcsoltId);
        r.setKapcsoltNev(u.kapcsoltNev);
        Felhasznalo f = felhasznaloRepo.findById(u.feladoId);
        r.setFeladoNev(f != null ? (f.nev != null ? f.nev : f.felhasznalonev) : "?");
        return r;
    }
}
