package hu.ingatlan.service;

import hu.ingatlan.domain.Ingatlan;
import hu.ingatlan.domain.Megbizas;
import hu.ingatlan.domain.Ugyfel;
import hu.ingatlan.dto.MegbizasDto;
import hu.ingatlan.repository.IngatlanRepository;
import hu.ingatlan.repository.MegbizasRepository;
import hu.ingatlan.repository.UgyfelRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class MegbizasService {

    @Inject MegbizasRepository repository;
    @Inject UgyfelRepository ugyfelRepository;
    @Inject IngatlanRepository ingatlanRepository;
    @Inject IrodaContext ctx;

    public List<MegbizasDto.Response> listAll() {
        if (ctx.isAdmin()) {
            return repository.listAll(io.quarkus.panache.common.Sort.by("letrehozva").descending())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        return repository.listByIroda(ctx.irodaIdOrThrow()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<MegbizasDto.Response> listAktivak() {
        if (ctx.isAdmin()) {
            return repository.findAktivak().stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        return repository.findAktivakByIroda(ctx.irodaIdOrThrow()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<MegbizasDto.Response> findByUgyfel(UUID ugyfelId) {
        if (ctx.isAdmin()) {
            return repository.findByUgyfel(ugyfelId).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        return repository.findByUgyfelAndIroda(ugyfelId, ctx.irodaIdOrThrow()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<MegbizasDto.Response> findByIngatlan(UUID ingatlanId) {
        if (ctx.isAdmin()) {
            return repository.findByIngatlan(ingatlanId).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        return repository.findByIngatlanAndIroda(ingatlanId, ctx.irodaIdOrThrow()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public MegbizasDto.Response findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public MegbizasDto.Response create(MegbizasDto.Request dto) {
        UUID irodaId = ctx.irodaIdOrThrow();

        Ugyfel ugyfel = ugyfelRepository.findById(dto.getUgyfelId());
        if (ugyfel == null || !irodaId.equals(ugyfel.irodaId))
            throw new NotFoundException("Ügyfél nem található: " + dto.getUgyfelId());

        Ingatlan ingatlan = ingatlanRepository.findById(dto.getIngatlanId());
        if (ingatlan == null || !irodaId.equals(ingatlan.irodaId))
            throw new NotFoundException("Ingatlan nem található: " + dto.getIngatlanId());

        Megbizas m = new Megbizas();
        m.irodaId = irodaId;
        m.ugyfel = ugyfel;
        m.ingatlan = ingatlan;
        applyDto(m, dto);
        repository.persist(m);
        return toResponse(m);
    }

    @Transactional
    public MegbizasDto.Response update(UUID id, MegbizasDto.Request dto) {
        Megbizas m = getOrThrow(id);
        // ADMIN esetén az entitás saját irodaId-ját használjuk az ellenőrzéshez
        UUID irodaId = ctx.isAdmin() ? m.irodaId : ctx.irodaIdOrThrow();

        if (!m.ugyfel.id.equals(dto.getUgyfelId())) {
            Ugyfel ugyfel = ugyfelRepository.findById(dto.getUgyfelId());
            if (ugyfel == null || !irodaId.equals(ugyfel.irodaId))
                throw new NotFoundException("Ügyfél nem található: " + dto.getUgyfelId());
            m.ugyfel = ugyfel;
        }
        if (!m.ingatlan.id.equals(dto.getIngatlanId())) {
            Ingatlan ingatlan = ingatlanRepository.findById(dto.getIngatlanId());
            if (ingatlan == null || !irodaId.equals(ingatlan.irodaId))
                throw new NotFoundException("Ingatlan nem található: " + dto.getIngatlanId());
            m.ingatlan = ingatlan;
        }
        applyDto(m, dto);
        return toResponse(m);
    }

    @Transactional
    public MegbizasDto.Response changeStatus(UUID id, Megbizas.MegbizasStatus newStatus) {
        Megbizas m = getOrThrow(id);
        m.status = newStatus;
        return toResponse(m);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(getOrThrow(id));
    }

    private void applyDto(Megbizas m, MegbizasDto.Request dto) {
        m.tipus = dto.getTipus();
        m.kezdete = dto.getKezdete();
        m.vege = dto.getVege();
        m.jutalekSzazalek = dto.getJutalekSzazalek();
    }

    private Megbizas getOrThrow(UUID id) {
        Megbizas m = repository.findById(id);
        if (m == null) throw new NotFoundException("Megbízás nem található: " + id);
        UUID irodaId = ctx.irodaId();
        if (irodaId != null && !irodaId.equals(m.irodaId))
            throw new NotFoundException("Megbízás nem található: " + id);
        return m;
    }

    private MegbizasDto.Response toResponse(Megbizas m) {
        MegbizasDto.Response r = new MegbizasDto.Response();
        r.setId(m.id);
        r.setUgyfelId(m.ugyfel.id);
        r.setUgyfelNev(m.ugyfel.nev);
        r.setIngatlanId(m.ingatlan.id);
        r.setIngatlanCim(m.ingatlan.cim);
        r.setTipus(m.tipus);
        r.setKezdete(m.kezdete);
        r.setVege(m.vege);
        r.setJutalekSzazalek(m.jutalekSzazalek);
        r.setStatus(m.status);
        r.setLetrehozva(m.letrehozva);
        return r;
    }
}
