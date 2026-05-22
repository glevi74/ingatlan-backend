package hu.ingatlan.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hu.ingatlan.domain.Felhasznalo;
import hu.ingatlan.domain.Iroda;
import hu.ingatlan.dto.AuthDto;
import hu.ingatlan.repository.FelhasznaloRepository;
import hu.ingatlan.repository.IrodaRepository;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class AuthService {

    @Inject FelhasznaloRepository repository;
    @Inject IrodaRepository irodaRepository;
    @Inject IrodaContext ctx;

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

        // JWT összeállítása – iroda_id hozzáadva
        var tokenBuilder = Jwt.issuer(issuer)
                .subject(f.id.toString())
                .claim("felhasznalonev", f.felhasznalonev)
                .claim("nev", f.nev != null ? f.nev : f.felhasznalonev)
                .groups(Set.of(f.szerep.name()))
                .expiresIn(Duration.ofHours(8));

        if (f.irodaId != null) {
            tokenBuilder.claim("iroda_id", f.irodaId.toString());
        }

        String token = tokenBuilder.sign();

        AuthDto.TokenResponse r = new AuthDto.TokenResponse();
        r.setToken(token);
        r.setFelhasznalonev(f.felhasznalonev);
        r.setNev(f.nev != null ? f.nev : f.felhasznalonev);
        r.setSzerep(f.szerep);
        r.setIrodaId(f.irodaId);

        // Iroda adatok hozzáfűzése a válaszhoz
        if (f.irodaId != null) {
            Iroda iroda = irodaRepository.findById(f.irodaId);
            if (iroda != null) {
                r.setIrodaNev(iroda.nev);
                r.setIrodaSzinElsodleges(iroda.szinElsodleges);
                r.setIrodaSzinMasodlagos(iroda.szinMasodlagos);
            }
        }
        return r;
    }

    @Transactional
    public void regisztracio(AuthDto.RegisztracioRequest req) {
        if (repository.findByFelhasznalonev(req.getFelhasznalonev()) != null) {
            throw new WebApplicationException(
                    "Ez a felhasználónév már foglalt: " + req.getFelhasznalonev(),
                    Response.Status.CONFLICT);
        }

        // IRODAVEZETO csak saját irodájába vehet fel felhasználót
        UUID irodaId = resolveIrodaId(req);

        Felhasznalo f = new Felhasznalo();
        f.felhasznalonev = req.getFelhasznalonev();
        f.jelszoHash = BCrypt.withDefaults().hashToString(12, req.getJelszo().toCharArray());
        f.szerep = req.getSzerep();
        f.irodaId = irodaId;
        f.nev = req.getNev();
        f.email = req.getEmail();
        f.telefon = req.getTelefon();
        repository.persist(f);
    }

    /**
     * Meghatározza, hogy az új felhasználó melyik irodához tartozzon.
     * - ADMIN létrehozhat bármilyen szerepkörű felhasználót bármely irodában.
     * - IRODAVEZETO csak saját irodájában hozhat létre felhasználókat.
     */
    private java.util.UUID resolveIrodaId(AuthDto.RegisztracioRequest req) {
        boolean hivo_admin = ctx.isAdmin();
        boolean hivo_vezeto = ctx.isVezeto() && !hivo_admin;

        if (req.getSzerep() == Felhasznalo.FelhasznaloSzerep.ADMIN) {
            // ADMIN-t csak ADMIN hozhat létre, és nincs irodája
            if (!hivo_admin) throw new ForbiddenException("ADMIN szerepkört csak rendszer-adminisztrátor hozhat létre.");
            return null;
        }

        if (hivo_admin) {
            // ADMIN megadhatja az iroda_id-t a requestben
            if (req.getIrodaId() == null)
                throw new WebApplicationException("IRODAVEZETO/REFERENS/ASSZISZTENS létrehozásához meg kell adni az irodaId-t.", Response.Status.BAD_REQUEST);
            if (irodaRepository.findById(req.getIrodaId()) == null)
                throw new WebApplicationException("Iroda nem található: " + req.getIrodaId(), Response.Status.NOT_FOUND);
            return req.getIrodaId();
        }

        if (hivo_vezeto) {
            // IRODAVEZETO csak saját irodájába vehet fel
            return ctx.irodaIdOrThrow();
        }

        throw new ForbiddenException("Nincs jogosultsága felhasználót létrehozni.");
    }
}
