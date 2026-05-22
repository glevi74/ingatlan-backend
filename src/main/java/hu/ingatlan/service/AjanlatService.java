package hu.ingatlan.service;

import hu.ingatlan.domain.Ajanlat;
import hu.ingatlan.domain.Megbizas;
import hu.ingatlan.domain.Ugyfel;
import hu.ingatlan.dto.AjanlatDto;
import hu.ingatlan.repository.AjanlatRepository;
import hu.ingatlan.repository.MegbizasRepository;
import hu.ingatlan.repository.UgyfelRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AjanlatService {

    @Inject AjanlatRepository repository;
    @Inject MegbizasRepository megbizasRepository;
    @Inject UgyfelRepository ugyfelRepository;
    @Inject IrodaContext ctx;

    public List<AjanlatDto.Response> listAll() {
        UUID irodaId = ctx.irodaId();
        if (irodaId == null) {
            return repository.listAll(io.quarkus.panache.common.Sort.by("letrehozva").descending())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        return repository.listByIroda(irodaId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<AjanlatDto.Response> findByMegbizas(UUID megbizasId) {
        UUID irodaId = ctx.irodaId();
        if (irodaId == null) {
            return repository.findByMegbizas(megbizasId).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        return repository.findByMegbizasAndIroda(megbizasId, irodaId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<AjanlatDto.Response> findByUgyfel(UUID ugyfelId) {
        UUID irodaId = ctx.irodaId();
        if (irodaId == null) {
            return repository.findByUgyfel(ugyfelId).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        return repository.findByUgyfelAndIroda(ugyfelId, irodaId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public AjanlatDto.Response findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public AjanlatDto.Response create(AjanlatDto.Request dto) {
        UUID irodaId = ctx.irodaIdOrThrow();

        Megbizas megbizas = megbizasRepository.findById(dto.getMegbizasId());
        if (megbizas == null || !irodaId.equals(megbizas.irodaId))
            throw new NotFoundException("Megbízás nem található: " + dto.getMegbizasId());

        Ugyfel ugyfel = ugyfelRepository.findById(dto.getUgyfelId());
        if (ugyfel == null || !irodaId.equals(ugyfel.irodaId))
            throw new NotFoundException("Ügyfél nem található: " + dto.getUgyfelId());

        Ajanlat a = new Ajanlat();
        a.irodaId = irodaId;
        a.megbizas = megbizas;
        a.ugyfel = ugyfel;
        applyDto(a, dto);
        repository.persist(a);
        return toResponse(a);
    }

    @Transactional
    public AjanlatDto.Response update(UUID id, AjanlatDto.Request dto) {
        Ajanlat a = getOrThrow(id);
        UUID irodaId = ctx.irodaId() != null ? ctx.irodaId() : a.irodaId;

        if (!a.megbizas.id.equals(dto.getMegbizasId())) {
            Megbizas megbizas = megbizasRepository.findById(dto.getMegbizasId());
            if (megbizas == null || !irodaId.equals(megbizas.irodaId))
                throw new NotFoundException("Megbízás nem található: " + dto.getMegbizasId());
            a.megbizas = megbizas;
        }
        if (!a.ugyfel.id.equals(dto.getUgyfelId())) {
            Ugyfel ugyfel = ugyfelRepository.findById(dto.getUgyfelId());
            if (ugyfel == null || !irodaId.equals(ugyfel.irodaId))
                throw new NotFoundException("Ügyfél nem található: " + dto.getUgyfelId());
            a.ugyfel = ugyfel;
        }
        applyDto(a, dto);
        return toResponse(a);
    }

    @Transactional
    public AjanlatDto.Response changeStatus(UUID id, Ajanlat.AjanlatStatus newStatus) {
        Ajanlat a = getOrThrow(id);
        if (newStatus == Ajanlat.AjanlatStatus.ELFOGADOTT) {
            throw new BadRequestException("Elfogadáshoz használd a /elfogad végpontot.");
        }
        a.status = newStatus;
        return toResponse(a);
    }

    @Transactional
    public AjanlatDto.Response elfogad(UUID id) {
        Ajanlat elfogadott = getOrThrow(id);

        if (elfogadott.status == Ajanlat.AjanlatStatus.ELFOGADOTT) {
            throw new BadRequestException("Ez az ajánlat már el van fogadva.");
        }
        if (elfogadott.megbizas.status != Megbizas.MegbizasStatus.AKTIV) {
            throw new BadRequestException("Csak aktív megbízáshoz tartozó ajánlat fogadható el.");
        }

        // A többi ajánlat elutasítása ugyanarra a megbízásra
        UUID irodaId = ctx.irodaId();
        (irodaId == null
            ? repository.findByMegbizas(elfogadott.megbizas.id)
            : repository.findByMegbizasAndIroda(elfogadott.megbizas.id, irodaId)
        ).stream()
                .filter(a -> !a.id.equals(id))
                .filter(a -> a.status == Ajanlat.AjanlatStatus.BEERKEZETT
                        || a.status == Ajanlat.AjanlatStatus.TARGYALASBAN)
                .forEach(a -> a.status = Ajanlat.AjanlatStatus.ELUTASITOTT);

        elfogadott.status = Ajanlat.AjanlatStatus.ELFOGADOTT;
        elfogadott.megbizas.status = Megbizas.MegbizasStatus.LEZART;

        return toResponse(elfogadott);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(getOrThrow(id));
    }

    private void applyDto(Ajanlat a, AjanlatDto.Request dto) {
        a.ajanlottAr = dto.getAjanlottAr();
        a.datum = dto.getDatum();
        a.megjegyzes = dto.getMegjegyzes();
    }

    private Ajanlat getOrThrow(UUID id) {
        Ajanlat a = repository.findById(id);
        if (a == null) throw new NotFoundException("Ajánlat nem található: " + id);
        UUID irodaId = ctx.irodaId();
        if (irodaId != null && !irodaId.equals(a.irodaId))
            throw new NotFoundException("Ajánlat nem található: " + id);
        return a;
    }

    AjanlatDto.Response toResponse(Ajanlat a) {
        AjanlatDto.Response r = new AjanlatDto.Response();
        r.setId(a.id);
        r.setIrodaId(a.irodaId);
        r.setMegbizasId(a.megbizas.id);
        r.setIngatlanCim(a.megbizas.ingatlan.cim);
        r.setUgyfelId(a.ugyfel.id);
        r.setUgyfelNev(a.ugyfel.nev);
        r.setAjanlottAr(a.ajanlottAr);
        r.setDatum(a.datum);
        r.setStatus(a.status);
        r.setMegjegyzes(a.megjegyzes);
        r.setSzerzesId(a.szerzes != null ? a.szerzes.id : null);
        r.setLetrehozva(a.letrehozva);
        return r;
    }
}
