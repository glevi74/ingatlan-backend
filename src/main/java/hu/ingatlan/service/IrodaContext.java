package hu.ingatlan.service;

import hu.ingatlan.domain.Felhasznalo;
import io.quarkus.security.identity.SecurityIdentity;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

/**
 * Request-scoped bean: a JWT tokenből kinyeri az aktuálisan bejelentkezett
 * felhasználó iroda_id-ját és szerepkörét.
 * Minden service ezt injektálja a multitenancy szűréshez.
 * <p>
 * ADMIN esetén az iroda_id a JWT-ben null; a kliens az X-Iroda-Id HTTP fejlécben
 * adhatja meg, hogy melyik iroda kontextusában dolgozik.
 */
@RequestScoped
public class IrodaContext {

    @Inject
    JsonWebToken jwt;

    @Inject
    SecurityIdentity identity;

    @Inject
    RoutingContext routingContext;

    /**
     * Az aktuális felhasználó iroda UUID-ja.
     * <ul>
     *   <li>Nem-ADMIN felhasználóknál: a JWT {@code iroda_id} claim értéke.</li>
     *   <li>ADMIN felhasználóknál: az {@code X-Iroda-Id} HTTP fejléc értéke
     *       (ha meg van adva), különben {@code null}.</li>
     * </ul>
     */
    public UUID irodaId() {
        String raw = jwt.getClaim("iroda_id");
        if (raw != null && !raw.isBlank()) return UUID.fromString(raw);
        // ADMIN esetén: opcionális X-Iroda-Id fejléc
        try {
            String header = routingContext.request().getHeader("X-Iroda-Id");
            if (header != null && !header.isBlank()) return UUID.fromString(header);
        } catch (Exception ignored) {
            // RoutingContext nem elérhető (pl. ütemezett task) – nincs baj
        }
        return null;
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
