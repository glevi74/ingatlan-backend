package hu.ingatlan.service;

import hu.ingatlan.domain.Feladat;
import hu.ingatlan.domain.Felhasznalo;
import hu.ingatlan.dto.FeladatDto;
import hu.ingatlan.repository.FeladatRepository;
import hu.ingatlan.repository.FelhasznaloRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class FeladatService {

    @Inject FeladatRepository repository;
    @Inject FelhasznaloRepository felhasznaloRepository;
    @Inject IrodaContext ctx;

    public List<FeladatDto.Response> listAll(Feladat.FeladatStatus status, UUID hozzarendeltId) {
        UUID irodaId = ctx.irodaIdOrThrow();
        List<Feladat> feladatok;

        if (status != null) {
            feladatok = repository.listByIrodaAndStatus(irodaId, status);
        } else if (hozzarendeltId != null) {
            feladatok = repository.listByIrodaAndHozzarendelt(irodaId, hozzarendeltId);
        } else {
            feladatok = repository.listByIroda(irodaId);
        }

        return feladatok.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public FeladatDto.Response findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public FeladatDto.Response create(FeladatDto.Request dto) {
        UUID irodaId = ctx.irodaIdOrThrow();
        Feladat f = new Feladat();
        f.irodaId = irodaId;
        f.letrehozoId = ctx.felhasznaloId();
        applyDto(f, dto, irodaId);
        repository.persist(f);
        return toResponse(f);
    }

    @Transactional
    public FeladatDto.Response update(UUID id, FeladatDto.Request dto) {
        Feladat f = getOrThrow(id);
        applyDto(f, dto, f.irodaId);
        return toResponse(f);
    }

    @Transactional
    public FeladatDto.Response changeStatus(UUID id, Feladat.FeladatStatus newStatus) {
        Feladat f = getOrThrow(id);
        f.status = newStatus;
        return toResponse(f);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(getOrThrow(id));
    }

    // ───────── helpers ─────────────────────────────────────────────────────

    private void applyDto(Feladat f, FeladatDto.Request dto, UUID irodaId) {
        f.cim = dto.getCim();
        f.leiras = dto.getLeiras();
        f.hatarido = dto.getHatarido();
        f.prioritas = dto.getPrioritas() != null ? dto.getPrioritas() : Feladat.FeladatPrioritas.NORMAL;

        // Hozzárendelt validálása: csak saját iroda felhasználója lehet
        if (dto.getHozzarendeltId() != null) {
            Felhasznalo hozzarendelt = felhasznaloRepository.findById(dto.getHozzarendeltId());
            if (hozzarendelt == null || !irodaId.equals(hozzarendelt.irodaId)) {
                throw new NotFoundException("Felhasználó nem található ebben az irodában: " + dto.getHozzarendeltId());
            }
            f.hozzarendeltId = dto.getHozzarendeltId();
        } else {
            f.hozzarendeltId = null;
        }
    }

    private Feladat getOrThrow(UUID id) {
        Feladat f = repository.findById(id);
        if (f == null) throw new NotFoundException("Feladat nem található: " + id);
        if (!ctx.irodaIdOrThrow().equals(f.irodaId)) {
            throw new NotFoundException("Feladat nem található: " + id);
        }
        return f;
    }

    private FeladatDto.Response toResponse(Feladat f) {
        FeladatDto.Response r = new FeladatDto.Response();
        r.setId(f.id);
        r.setCim(f.cim);
        r.setLeiras(f.leiras);
        r.setHatarido(f.hatarido);
        r.setPrioritas(f.prioritas);
        r.setStatus(f.status);
        r.setIrodaId(f.irodaId);
        r.setLetrehozva(f.letrehozva);
        r.setModositva(f.modositva);

        if (f.hozzarendeltId != null) {
            r.setHozzarendeltId(f.hozzarendeltId);
            Felhasznalo h = felhasznaloRepository.findById(f.hozzarendeltId);
            if (h != null) r.setHozzarendeltNev(h.nev != null ? h.nev : h.felhasznalonev);
        }
        if (f.letrehozoId != null) {
            r.setLetrehozoId(f.letrehozoId);
            Felhasznalo l = felhasznaloRepository.findById(f.letrehozoId);
            if (l != null) r.setLetrehozoNev(l.nev != null ? l.nev : l.felhasznalonev);
        }
        return r;
    }
}
