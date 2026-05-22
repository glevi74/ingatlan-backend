package hu.ingatlan.service;

import hu.ingatlan.domain.Ingatlan;
import hu.ingatlan.dto.IngatlanDto;
import hu.ingatlan.repository.IngatlanRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class IngatlanService {

    @Inject
    IngatlanRepository repository;

    @Inject
    IrodaContext ctx;

    public List<IngatlanDto.Response> listAll() {
        if (ctx.isAdmin()) {
            return repository.listAll(io.quarkus.panache.common.Sort.by("letrehozva").descending())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        return repository.listByIroda(ctx.irodaIdOrThrow()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<IngatlanDto.Response> search(IngatlanDto.SearchParams params) {
        // ADMIN esetén nincs iroda-szűrés (null átadva)
        UUID irodaId = ctx.isAdmin() ? null : ctx.irodaIdOrThrow();
        return repository.search(
                irodaId,
                params.getTipus(),
                params.getMinAlapterulet(),
                params.getMaxAlapterulet(),
                params.getMinSzobaszam()
        ).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public IngatlanDto.Response findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public IngatlanDto.Response create(IngatlanDto.Request dto) {
        Ingatlan i = new Ingatlan();
        i.irodaId = ctx.irodaIdOrThrow();
        applyDto(i, dto);
        repository.persist(i);
        return toResponse(i);
    }

    @Transactional
    public IngatlanDto.Response update(UUID id, IngatlanDto.Request dto) {
        Ingatlan i = getOrThrow(id);
        applyDto(i, dto);
        return toResponse(i);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(getOrThrow(id));
    }

    private void applyDto(Ingatlan i, IngatlanDto.Request dto) {
        i.cim = dto.getCim();
        i.helyrajziSzam = dto.getHelyrajziSzam();
        i.tipus = dto.getTipus();
        i.alapterulet = dto.getAlapterulet();
        i.telekterulet = dto.getTelekterulet();
        i.szobaszam = dto.getSzobaszam();
        i.emelet = dto.getEmelet();
        i.allapot = dto.getAllapot();
        i.energetikaiOsztaly = dto.getEnergetikaiOsztaly();
        i.jogiStatus = dto.getJogiStatus();
        i.leiras = dto.getLeiras();
    }

    private Ingatlan getOrThrow(UUID id) {
        Ingatlan i = repository.findById(id);
        if (i == null) throw new NotFoundException("Ingatlan nem található: " + id);
        // Iroda-ellenőrzés: csak saját iroda adatát láthatja
        UUID irodaId = ctx.irodaId();
        if (irodaId != null && !irodaId.equals(i.irodaId)) {
            throw new NotFoundException("Ingatlan nem található: " + id);
        }
        return i;
    }

    private IngatlanDto.Response toResponse(Ingatlan i) {
        IngatlanDto.Response r = new IngatlanDto.Response();
        r.setId(i.id);
        r.setCim(i.cim);
        r.setHelyrajziSzam(i.helyrajziSzam);
        r.setTipus(i.tipus);
        r.setAlapterulet(i.alapterulet);
        r.setTelekterulet(i.telekterulet);
        r.setSzobaszam(i.szobaszam);
        r.setEmelet(i.emelet);
        r.setAllapot(i.allapot);
        r.setEnergetikaiOsztaly(i.energetikaiOsztaly);
        r.setJogiStatus(i.jogiStatus);
        r.setLeiras(i.leiras);
        r.setLetrehozva(i.letrehozva);
        return r;
    }
}
