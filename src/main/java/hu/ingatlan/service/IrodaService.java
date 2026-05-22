package hu.ingatlan.service;

import hu.ingatlan.domain.Iroda;
import hu.ingatlan.dto.IrodaDto;
import hu.ingatlan.repository.IrodaRepository;
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
public class IrodaService {

    @Inject
    IrodaRepository repository;

    @Inject
    IrodaContext ctx;

    public List<IrodaDto.Response> listAll() {
        return repository.listAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public IrodaDto.Response findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public IrodaDto.Response findSajat() {
        UUID irodaId = ctx.irodaIdOrThrow();
        return toResponse(getOrThrow(irodaId));
    }

    @Transactional
    public IrodaDto.Response create(IrodaDto.Request dto) {
        if (repository.slugFoglalt(dto.getSlug(), null)) {
            throw new WebApplicationException("Ez a slug már foglalt: " + dto.getSlug(),
                    Response.Status.CONFLICT);
        }
        Iroda i = new Iroda();
        applyDto(i, dto);
        repository.persist(i);
        return toResponse(i);
    }

    @Transactional
    public IrodaDto.Response update(UUID id, IrodaDto.Request dto) {
        Iroda i = getOrThrow(id);

        // Ha nem admin, csak a saját irodáját módosíthatja
        if (!ctx.isAdmin() && !id.equals(ctx.irodaId())) {
            throw new WebApplicationException("Csak a saját iroda adatai módosíthatók.",
                    Response.Status.FORBIDDEN);
        }

        if (repository.slugFoglalt(dto.getSlug(), id)) {
            throw new WebApplicationException("Ez a slug már foglalt: " + dto.getSlug(),
                    Response.Status.CONFLICT);
        }
        applyDto(i, dto);
        return toResponse(i);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(getOrThrow(id));
    }

    private void applyDto(Iroda i, IrodaDto.Request dto) {
        i.nev = dto.getNev();
        i.slug = dto.getSlug();
        i.leiras = dto.getLeiras();
        i.logoUrl = dto.getLogoUrl();
        i.telefon = dto.getTelefon();
        i.email = dto.getEmail();
        i.cim = dto.getCim();
        i.weboldal = dto.getWeboldal();
        if (dto.getSzinElsodleges() != null) i.szinElsodleges = dto.getSzinElsodleges();
        if (dto.getSzinMasodlagos() != null) i.szinMasodlagos = dto.getSzinMasodlagos();
    }

    private Iroda getOrThrow(UUID id) {
        Iroda i = repository.findById(id);
        if (i == null) throw new NotFoundException("Iroda nem található: " + id);
        return i;
    }

    public IrodaDto.Response toResponse(Iroda i) {
        IrodaDto.Response r = new IrodaDto.Response();
        r.setId(i.id);
        r.setNev(i.nev);
        r.setSlug(i.slug);
        r.setLeiras(i.leiras);
        r.setLogoUrl(i.logoUrl);
        r.setTelefon(i.telefon);
        r.setEmail(i.email);
        r.setCim(i.cim);
        r.setWeboldal(i.weboldal);
        r.setSzinElsodleges(i.szinElsodleges);
        r.setSzinMasodlagos(i.szinMasodlagos);
        r.setAktiv(i.aktiv);
        r.setLetrehozva(i.letrehozva);
        return r;
    }
}
