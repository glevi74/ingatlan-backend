package hu.ingatlan.service;

import hu.ingatlan.domain.Hirdetes;
import hu.ingatlan.domain.Megbizas;
import hu.ingatlan.dto.HirdetesDto;
import hu.ingatlan.repository.HirdetesRepository;
import hu.ingatlan.repository.MegbizasRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class HirdetesService {

    @Inject HirdetesRepository repository;
    @Inject MegbizasRepository megbizasRepository;
    @Inject IrodaContext ctx;

    public List<HirdetesDto.Response> listAll() {
        if (ctx.isAdmin()) {
            return repository.listAll(io.quarkus.panache.common.Sort.by("letrehozva").descending())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        return repository.listByIroda(ctx.irodaIdOrThrow()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<HirdetesDto.Response> listAktivak() {
        if (ctx.isAdmin()) {
            return repository.findAktivak().stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        return repository.findAktivakByIroda(ctx.irodaIdOrThrow()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<HirdetesDto.Response> findByMegbizas(UUID megbizasId) {
        if (ctx.isAdmin()) {
            return repository.findByMegbizas(megbizasId).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        return repository.findByMegbizasAndIroda(megbizasId, ctx.irodaIdOrThrow()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public HirdetesDto.Response findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public HirdetesDto.Response create(HirdetesDto.Request dto) {
        UUID irodaId = ctx.irodaIdOrThrow();
        Megbizas megbizas = megbizasRepository.findById(dto.getMegbizasId());
        if (megbizas == null || !irodaId.equals(megbizas.irodaId))
            throw new NotFoundException("Megbízás nem található: " + dto.getMegbizasId());

        Hirdetes h = new Hirdetes();
        h.irodaId = irodaId;
        h.megbizas = megbizas;
        applyDto(h, dto);
        repository.persist(h);
        return toResponse(h);
    }

    @Transactional
    public HirdetesDto.Response update(UUID id, HirdetesDto.Request dto) {
        Hirdetes h = getOrThrow(id);
        // ADMIN esetén az entitás saját irodaId-ját használjuk az ellenőrzéshez
        UUID irodaId = ctx.isAdmin() ? h.irodaId : ctx.irodaIdOrThrow();

        if (!h.megbizas.id.equals(dto.getMegbizasId())) {
            Megbizas megbizas = megbizasRepository.findById(dto.getMegbizasId());
            if (megbizas == null || !irodaId.equals(megbizas.irodaId))
                throw new NotFoundException("Megbízás nem található: " + dto.getMegbizasId());
            h.megbizas = megbizas;
        }
        applyDto(h, dto);
        return toResponse(h);
    }

    @Transactional
    public HirdetesDto.Response changeStatus(UUID id, Hirdetes.HirdetesStatus newStatus) {
        Hirdetes h = getOrThrow(id);
        h.status = newStatus;
        return toResponse(h);
    }

    @Transactional
    public HirdetesDto.Response megtekintesNoveles(UUID id) {
        Hirdetes h = getOrThrow(id);
        h.megtekintesek = (h.megtekintesek == null ? 0 : h.megtekintesek) + 1;
        return toResponse(h);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(getOrThrow(id));
    }

    private void applyDto(Hirdetes h, HirdetesDto.Request dto) {
        h.kerAr = dto.getKerAr();
        h.portal = dto.getPortal();
        h.indulas = dto.getIndulas();
        h.lejaras = dto.getLejaras();
    }

    private Hirdetes getOrThrow(UUID id) {
        Hirdetes h = repository.findById(id);
        if (h == null) throw new NotFoundException("Hirdetés nem található: " + id);
        UUID irodaId = ctx.irodaId();
        if (irodaId != null && !irodaId.equals(h.irodaId))
            throw new NotFoundException("Hirdetés nem található: " + id);
        return h;
    }

    private HirdetesDto.Response toResponse(Hirdetes h) {
        HirdetesDto.Response r = new HirdetesDto.Response();
        r.setId(h.id);
        r.setMegbizasId(h.megbizas.id);
        r.setIngatlanCim(h.megbizas.ingatlan.cim);
        r.setKerAr(h.kerAr);
        r.setPortal(h.portal);
        r.setStatus(h.status);
        r.setIndulas(h.indulas);
        r.setLejaras(h.lejaras);
        r.setMegtekintesek(h.megtekintesek);
        r.setLetrehozva(h.letrehozva);
        return r;
    }
}
