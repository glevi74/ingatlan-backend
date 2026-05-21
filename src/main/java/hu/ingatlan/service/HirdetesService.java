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

    @Inject
    HirdetesRepository repository;

    @Inject
    MegbizasRepository megbizasRepository;

    public List<HirdetesDto.Response> listAll() {
        return repository.listAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<HirdetesDto.Response> listAktivak() {
        return repository.findAktivak().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<HirdetesDto.Response> findByMegbizas(UUID megbizasId) {
        return repository.findByMegbizas(megbizasId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public HirdetesDto.Response findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public HirdetesDto.Response create(HirdetesDto.Request dto) {
        Megbizas megbizas = megbizasRepository.findById(dto.getMegbizasId());
        if (megbizas == null) throw new NotFoundException("Megbízás nem található: " + dto.getMegbizasId());

        Hirdetes h = new Hirdetes();
        h.megbizas = megbizas;
        applyDto(h, dto);
        repository.persist(h);
        return toResponse(h);
    }

    @Transactional
    public HirdetesDto.Response update(UUID id, HirdetesDto.Request dto) {
        Hirdetes h = getOrThrow(id);

        if (!h.megbizas.id.equals(dto.getMegbizasId())) {
            Megbizas megbizas = megbizasRepository.findById(dto.getMegbizasId());
            if (megbizas == null) throw new NotFoundException("Megbízás nem található: " + dto.getMegbizasId());
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
