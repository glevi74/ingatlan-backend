package hu.ingatlan.service;

import hu.ingatlan.domain.Felhasznalo;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

/**
 * Request-scoped bean: a JWT tokenből kinyeri az aktuálisan bejelentkezett
 * felhasználó iroda_id-ját és szerepkörét.
 * Minden service ezt injektálja a multitenancy szűréshez.
 */
@RequestScoped
public class IrodaContext {

    @Inject
    JsonWebToken jwt;

    @Inject
    SecurityIdentity identity;

    /** Az aktuális felhasználó iroda UUID-ja (ADMIN esetén null). */
    public UUID irodaId() {
        String raw = jwt.getClaim("iroda_id");
        if (raw == null || raw.isBlank()) return null;
        return UUID.fromString(raw);
    }

    /** Az aktuális felhasználó UUID-ja (a token sub mezőjéből). */
    public UUID felhasznaloId() {
        return UUID.fromString(jwt.getSubject());
    }

    /** Igaz, ha a bejelentkezett felhasználó rendszer-szintű ADMIN. */
    public boolean isAdmin() {
        return identity.hasRole(Felhasznalo.FelhasznaloSzerep.ADMIN.name());
    }

    /** Igaz, ha a bejelentkezett felhasználó IRODAVEZETO vagy ADMIN. */
    public boolean isVezeto() {
        return isAdmin() || identity.hasRole(Felhasznalo.FelhasznaloSzerep.IRODAVEZETO.name());
    }

    /**
     * Visszaadja az iroda UUID-ját, vagy kivételt dob, ha a felhasználónak
     * nincs irodája (pl. ADMIN-nak nem szükséges, de egy normál lekérdezésnél igen).
     */
    public UUID irodaIdOrThrow() {
        UUID id = irodaId();
        if (id == null) throw new ForbiddenException("Ehhez a művelethez iroda-kontextus szükséges.");
        return id;
    }
}
