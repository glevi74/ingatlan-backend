package hu.ingatlan.service;

import hu.ingatlan.domain.Felhasznalo;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.UUID;

/**
 * Teszt során az IrodaContext helyettesítője (CDI alternative, Priority=1).
 * <p>
 * Rögzített {@link #TEST_IRODA_ID} UUID-ot ad vissza minden hívásra,
 * ezért a tesztek nem függnek valós JWT-tokentől.
 * Az admin/vezető szerepkör-ellenőrzés a SecurityIdentity alapján működik,
 * amit a {@code @TestSecurity} annotáció állít be.
 */
@Mock
@RequestScoped
public class TestIrodaContext extends IrodaContext {

    /** Az összes teszt által használt rögzített iroda-azonosító. */
    public static final UUID TEST_IRODA_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Inject
    SecurityIdentity identity;

    @Override
    public UUID irodaId() {
        return TEST_IRODA_ID;
    }

    @Override
    public UUID irodaIdOrThrow() {
        return TEST_IRODA_ID;
    }

    @Override
    public UUID felhasznaloId() {
        return UUID.fromString("00000000-0000-0000-0000-000000000099");
    }

    @Override
    public boolean isAdmin() {
        return identity.hasRole(Felhasznalo.FelhasznaloSzerep.ADMIN.name());
    }

    @Override
    public boolean isVezeto() {
        return isAdmin() || identity.hasRole(Felhasznalo.FelhasznaloSzerep.IRODAVEZETO.name());
    }
}
