package hu.ingatlan.service;

import hu.ingatlan.domain.Ajanlat;
import hu.ingatlan.domain.Szerzes;
import hu.ingatlan.dto.SzerzesDto;
import hu.ingatlan.repository.AjanlatRepository;
import hu.ingatlan.repository.SzerzesRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class SzerzesService {

    @Inject SzerzesRepository repository;
    @Inject AjanlatRepository ajanlatRepository;
    @Inject IrodaContext ctx;

    public List<SzerzesDto.Response> listAll() {
        UUID irodaId = ctx.irodaId();
        if (irodaId == null) {
            return repository.listAll(io.quarkus.panache.common.Sort.by("letrehozva").descending())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        return repository.listByIroda(irodaId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public SzerzesDto.Response findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public SzerzesDto.Response findByAjanlat(UUID ajanlatId) {
        UUID irodaId = ctx.irodaId();
        if (irodaId == null) {
            return repository.findByAjanlat(ajanlatId)
                    .map(this::toResponse)
                    .orElseThrow(() -> new NotFoundException("Szerzés nem található ehhez az ajánlathoz: " + ajanlatId));
        }
        return repository.findByAjanlatAndIroda(ajanlatId, irodaId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Szerzés nem található ehhez az ajánlathoz: " + ajanlatId));
    }

    @Transactional
    public SzerzesDto.Response create(SzerzesDto.Request dto) {
        UUID irodaId = ctx.irodaIdOrThrow();

        Ajanlat ajanlat = ajanlatRepository.findById(dto.getAjanlatId());
        if (ajanlat == null || !irodaId.equals(ajanlat.irodaId))
            throw new NotFoundException("Ajánlat nem található: " + dto.getAjanlatId());

        if (ajanlat.status != Ajanlat.AjanlatStatus.ELFOGADOTT) {
            throw new BadRequestException("Szerzés csak elfogadott ajánlathoz rögzíthető.");
        }
        if (repository.findByAjanlatAndIroda(dto.getAjanlatId(), irodaId).isPresent()) {
            throw new BadRequestException("Ehhez az ajánlathoz már van rögzített szerzés.");
        }

        Szerzes sz = new Szerzes();
        sz.irodaId = irodaId;
        sz.ajanlat = ajanlat;
        applyDto(sz, dto);
        repository.persist(sz);
        return toResponse(sz);
    }

    @Transactional
    public SzerzesDto.Response update(UUID id, SzerzesDto.Request dto) {
        Szerzes sz = getOrThrow(id);
        applyDto(sz, dto);
        return toResponse(sz);
    }

    @Transactional
    public SzerzesDto.Response changeStatus(UUID id, Szerzes.SzerzesStatus newStatus) {
        Szerzes sz = getOrThrow(id);
        sz.status = newStatus;
        return toResponse(sz);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(getOrThrow(id));
    }

    private void applyDto(Szerzes sz, SzerzesDto.Request dto) {
        sz.vegsoAr = dto.getVegsoAr();
        sz.szerzodesDatum = dto.getSzerzodesDatum();
        sz.jutalekOsszeg = dto.getJutalekOsszeg();
        sz.megjegyzes = dto.getMegjegyzes();
    }

    private Szerzes getOrThrow(UUID id) {
        Szerzes sz = repository.findById(id);
        if (sz == null) throw new NotFoundException("Szerzés nem található: " + id);
        UUID irodaId = ctx.irodaId();
        if (irodaId != null && !irodaId.equals(sz.irodaId))
            throw new NotFoundException("Szerzés nem található: " + id);
        return sz;
    }

    private SzerzesDto.Response toResponse(Szerzes sz) {
        SzerzesDto.Response r = new SzerzesDto.Response();
        r.setId(sz.id);
        r.setIrodaId(sz.irodaId);
        r.setAjanlatId(sz.ajanlat.id);
        r.setIngatlanCim(sz.ajanlat.megbizas.ingatlan.cim);
        r.setUgyfelId(sz.ajanlat.ugyfel.id);
        r.setUgyfelNev(sz.ajanlat.ugyfel.nev);
        r.setVegsoAr(sz.vegsoAr);
        r.setSzerzodesDatum(sz.szerzodesDatum);
        r.setJutalekOsszeg(sz.jutalekOsszeg);
        r.setStatus(sz.status);
        r.setMegjegyzes(sz.megjegyzes);
        r.setLetrehozva(sz.letrehozva);
        return r;
    }
}
