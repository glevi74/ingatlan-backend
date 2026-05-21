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

    public List<UgyfelDto.Response> listAll() {
        return repository.listAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UgyfelDto.Response findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public UgyfelDto.Response create(UgyfelDto.Request dto) {
        if (repository.findByEmail(dto.getEmail()) != null) {
            throw new WebApplicationException(
                "Ez az e-mail cím már foglalt: " + dto.getEmail(),
                Response.Status.CONFLICT);
        }
        Ugyfel ugyfel = new Ugyfel();
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
        Ugyfel ugyfel = getOrThrow(id);
        repository.delete(ugyfel);
    }

    private Ugyfel getOrThrow(UUID id) {
        Ugyfel ugyfel = repository.findById(id);
        if (ugyfel == null) throw new NotFoundException("Ügyfél nem található: " + id);
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
