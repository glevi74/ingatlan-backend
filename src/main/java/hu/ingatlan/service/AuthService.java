package hu.ingatlan.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hu.ingatlan.domain.Felhasznalo;
import hu.ingatlan.dto.AuthDto;
import hu.ingatlan.repository.FelhasznaloRepository;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    @Inject
    FelhasznaloRepository repository;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    public AuthDto.TokenResponse bejelentkezes(AuthDto.BejelentkezesRequest req) {
        Felhasznalo f = repository.findByFelhasznalonev(req.getFelhasznalonev());

        if (f == null || !f.aktiv) {
            throw new NotAuthorizedException("Érvénytelen felhasználónév vagy jelszó.",
                    Response.status(401).build());
        }
        boolean helyes = BCrypt.verifyer()
                .verify(req.getJelszo().toCharArray(), f.jelszoHash)
                .verified;
        if (!helyes) {
            throw new NotAuthorizedException("Érvénytelen felhasználónév vagy jelszó.",
                    Response.status(401).build());
        }

        String token = Jwt.issuer(issuer)
                .subject(f.id.toString())
                .claim("felhasznalonev", f.felhasznalonev)
                .groups(Set.of(f.szerep.name()))
                .expiresIn(Duration.ofHours(8))
                .sign();

        AuthDto.TokenResponse r = new AuthDto.TokenResponse();
        r.setToken(token);
        r.setFelhasznalonev(f.felhasznalonev);
        r.setSzerep(f.szerep);
        return r;
    }

    @Transactional
    public void regisztracio(AuthDto.RegisztracioRequest req) {
        if (repository.findByFelhasznalonev(req.getFelhasznalonev()) != null) {
            throw new WebApplicationException(
                    "Ez a felhasználónév már foglalt: " + req.getFelhasznalonev(),
                    Response.Status.CONFLICT);
        }
        Felhasznalo f = new Felhasznalo();
        f.felhasznalonev = req.getFelhasznalonev();
        f.jelszoHash = BCrypt.withDefaults().hashToString(12, req.getJelszo().toCharArray());
        f.szerep = req.getSzerep();
        repository.persist(f);
    }
}
