package hu.ingatlan.service;

import hu.ingatlan.domain.Ugyfel;
import hu.ingatlan.dto.UgyfelDto;
import hu.ingatlan.repository.UgyfelRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UgyfelService {

    @Inject
    UgyfelRepository repository;

    @Inject
    IrodaContext ctx;

    public List<UgyfelDto.Response> listAll() {
        if (ctx.isAdmin()) {
            return repository.listAll(io.quarkus.panache.common.Sort.by("letrehozva").descending())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        return repository.listByIroda(ctx.irodaIdOrThrow()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UgyfelDto.Response findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public UgyfelDto.Response create(UgyfelDto.Request dto) {
        UUID irodaId = ctx.irodaIdOrThrow();
        if (repository.findByEmailAndIroda(dto.getEmail(), irodaId) != null) {
            throw new WebApplicationException(
                "Ez az e-mail cím már foglalt ebben az irodában: " + dto.getEmail(),
                Response.Status.CONFLICT);
        }
        Ugyfel ugyfel = new Ugyfel();
        ugyfel.irodaId = irodaId;
        ugyfel.nev = dto.getNev();
        ugyfel.email = dto.getEmail();
        ugyfel.telefon = dto.getTelefon();
        ugyfel.szerep = dto.getSzerep();
        ugyfel.gdprBeleegyezes = dto.getGdprBeleegyezes();
        repository.persist(ugyfel);
        return toResponse(ugyfel);
    }

    @Transactional
    public UgyfelDto.Response update(UUID id, UgyfelDto.Request dto) {
        Ugyfel ugyfel = getOrThrow(id);
        ugyfel.nev = dto.getNev();
        ugyfel.email = dto.getEmail();
        ugyfel.telefon = dto.getTelefon();
        ugyfel.szerep = dto.getSzerep();
        ugyfel.gdprBeleegyezes = dto.getGdprBeleegyezes();
        return toResponse(ugyfel);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(getOrThrow(id));
    }

    private Ugyfel getOrThrow(UUID id) {
        Ugyfel ugyfel = repository.findById(id);
        if (ugyfel == null) throw new NotFoundException("Ügyfél nem található: " + id);
        UUID irodaId = ctx.irodaId();
        if (irodaId != null && !irodaId.equals(ugyfel.irodaId)) {
            throw new NotFoundException("Ügyfél nem található: " + id);
        }
        return ugyfel;
    }

    private UgyfelDto.Response toResponse(Ugyfel u) {
        UgyfelDto.Response r = new UgyfelDto.Response();
        r.setId(u.id);
        r.setNev(u.nev);
        r.setEmail(u.email);
        r.setTelefon(u.telefon);
        r.setSzerep(u.szerep);
        r.setGdprBeleegyezes(u.gdprBeleegyezes);
        r.setLetrehozva(u.letrehozva);
        return r;
    }
}
