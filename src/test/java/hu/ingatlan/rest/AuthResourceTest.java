package hu.ingatlan.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Auth endpoint tesztek.
 *
 * Megjegyzés: a sikeres bejelentkezés teszteléséhez JWT kulcsok szükségesek.
 * Futtasd a generate-keys.sh szkriptet, majd add hozzá a test profilhoz:
 *   %test.smallrye.jwt.sign.key.location=privateKey.pem
 *   %test.mp.jwt.verify.publickey.location=publicKey.pem
 */
@QuarkusTest
class AuthResourceTest {

    private static final String BEJELENTKEZES = "/api/v1/auth/bejelentkezes";
    private static final String REGISZTRACIO = "/api/v1/auth/regisztracio";

    @Test
    void testHibasJelszo() {
        given().contentType(ContentType.JSON)
                .body(Map.of("felhasznalonev", "admin", "jelszo", "rossz_jelszo"))
                .when().post(BEJELENTKEZES)
                .then().statusCode(401);
    }

    @Test
    void testNemLetezoFelhasznalo() {
        given().contentType(ContentType.JSON)
                .body(Map.of("felhasznalonev", "nemletezik", "jelszo", "akarmijelszo"))
                .when().post(BEJELENTKEZES)
                .then().statusCode(401);
    }

    @Test
    void testHianyosAdatok() {
        // Jelszó nélkül → validációs hiba
        given().contentType(ContentType.JSON)
                .body(Map.of("felhasznalonev", "admin"))
                .when().post(BEJELENTKEZES)
                .then().statusCode(400);

        // Üres body → validációs hiba
        given().contentType(ContentType.JSON)
                .body("{}")
                .when().post(BEJELENTKEZES)
                .then().statusCode(400);
    }

    @Test
    void testRegisztracioNemElerheto_Hitelesites_Nelkul() {
        // Regisztrácio token nélkül → 401
        given().contentType(ContentType.JSON)
                .body(Map.of("felhasznalonev", "ujfelhasznalo", "jelszo", "Jelszo123", "szerep", "UGYNOK"))
                .when().post(REGISZTRACIO)
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testRegisztracioAdmin() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "felhasznalonev", "uj.ugynok.teszt",
                        "jelszo", "Jelszo123",
                        "szerep", "UGYNOK"))
                .when().post(REGISZTRACIO)
                .then().statusCode(201);

        // Duplikált felhasználónév → 409
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "felhasznalonev", "uj.ugynok.teszt",
                        "jelszo", "MasikJelszo123",
                        "szerep", "UGYNOK"))
                .when().post(REGISZTRACIO)
                .then().statusCode(409);
    }

    @Test
    @TestSecurity(user = "ugynok", roles = {"UGYNOK"})
    void testRegisztracioTiltottUgynoknek() {
        // UGYNOK szerepkörrel regisztrálás → 403
        given().contentType(ContentType.JSON)
                .body(Map.of("felhasznalonev", "nemszabad", "jelszo", "Jelszo123", "szerep", "UGYNOK"))
                .when().post(REGISZTRACIO)
                .then().statusCode(403);
    }
}
