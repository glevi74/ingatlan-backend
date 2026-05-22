package hu.ingatlan.service;

import hu.ingatlan.domain.Felhasznalo;
import hu.ingatlan.domain.Iroda;
import hu.ingatlan.dto.FelhasznaloDto;
import hu.ingatlan.repository.FelhasznaloRepository;
import hu.ingatlan.repository.IrodaRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class FelhasznaloService {

    @Inject FelhasznaloRepository repository;
    @Inject IrodaRepository irodaRepository;
    @Inject IrodaContext ctx;

    /**
     * ADMIN: az összes felhasználót látja iroda-névvel együtt.
     * IRODAVEZETO/REFERENS/ASSZISZTENS: csak a saját iroda felhasználóit.
     */
    public List<FelhasznaloDto.Response> listAll() {
        if (ctx.isAdmin()) {
            return repository.listAll(Sort.by("letrehozva").descending())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        return repository.listByIroda(ctx.irodaIdOrThrow())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public FelhasznaloDto.Response findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public FelhasznaloDto.Response update(UUID id, FelhasznaloDto.UpdateRequest dto) {
        Felhasznalo f = getOrThrow(id);
        // IRODAVEZETO nem módosíthat ADMIN-t és nem hozhat létre ADMIN szerepkört
        if (!ctx.isAdmin() && (f.szerep == Felhasznalo.FelhasznaloSzerep.ADMIN
                || dto.getSzerep() == Felhasznalo.FelhasznaloSzerep.ADMIN)) {
            throw new ForbiddenException("ADMIN szerepkörű felhasználót csak rendszer-adminisztrátor módosíthat.");
        }
        f.nev = dto.getNev();
        f.email = dto.getEmail();
        f.telefon = dto.getTelefon();
        f.szerep = dto.getSzerep();
        return toResponse(f);
    }

    @Transactional
    public FelhasznaloDto.Response setAktiv(UUID id, boolean aktiv) {
        Felhasznalo f = getOrThrow(id);
        if (!ctx.isAdmin() && f.szerep == Felhasznalo.FelhasznaloSzerep.ADMIN) {
            throw new ForbiddenException("ADMIN felhasználót csak rendszer-adminisztrátor tilthat le.");
        }
        f.aktiv = aktiv;
        return toResponse(f);
    }

    // ───────── helpers ─────────

    private Felhasznalo getOrThrow(UUID id) {
        Felhasznalo f = repository.findById(id);
        if (f == null) throw new NotFoundException("Felhasználó nem található: " + id);
        // Bérlő-ellenőrzés: nem ADMIN láthat csak saját iroda felhasználóit
        if (!ctx.isAdmin() && !ctx.irodaIdOrThrow().equals(f.irodaId)) {
            throw new NotFoundException("Felhasználó nem található: " + id);
        }
        return f;
    }

    private FelhasznaloDto.Response toResponse(Felhasznalo f) {
        FelhasznaloDto.Response r = new FelhasznaloDto.Response();
        r.setId(f.id);
        r.setFelhasznalonev(f.felhasznalonev);
        r.setNev(f.nev);
        r.setEmail(f.email);
        r.setTelefon(f.telefon);
        r.setSzerep(f.szerep);
        r.setIrodaId(f.irodaId);
        r.setAktiv(f.aktiv);
        r.setLetrehozva(f.letrehozva);
        if (f.irodaId != null) {
            Iroda iroda = irodaRepository.findById(f.irodaId);
            if (iroda != null) r.setIrodaNev(iroda.nev);
        }
        return r;
    }
}
