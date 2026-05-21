package hu.ingatlan.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestSecurity(user = "admin", roles = {"ADMIN"})
class UgyfelResourceTest {

    private static final String BASE = "/api/v1/ugyfelek";

    @Test
    void testTeljesCrud() {
        // Létrehozás
        String id = given().contentType(ContentType.JSON)
                .body(Map.of(
                        "nev", "Teszt Elek",
                        "email", "teszt.elek@test.hu",
                        "telefon", "+36301234567",
                        "szerep", "VEVO",
                        "gdprBeleegyezes", "2024-01-10"))
                .when().post(BASE)
                .then().statusCode(201)
                .body("nev", equalTo("Teszt Elek"))
                .body("email", equalTo("teszt.elek@test.hu"))
                .body("id", notNullValue())
                .extract().path("id");

        // Lekérdezés
        given().when().get(BASE + "/" + id)
                .then().statusCode(200)
                .body("szerep", equalTo("VEVO"));

        // Módosítás
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "nev", "Teszt Elek Módosítva",
                        "email", "teszt.elek.mod@test.hu",
                        "szerep", "ELADO"))
                .when().put(BASE + "/" + id)
                .then().statusCode(200)
                .body("nev", equalTo("Teszt Elek Módosítva"))
                .body("szerep", equalTo("ELADO"));

        // Törlés
        given().when().delete(BASE + "/" + id)
                .then().statusCode(204);

        // Törlés után nem található
        given().when().get(BASE + "/" + id)
                .then().statusCode(404);
    }

    @Test
    void testDuplicaltEmail() {
        Map<String, Object> body = Map.of(
                "nev", "Duplikált A",
                "email", "dupli.ugyfel@test.hu",
                "szerep", "VEVO");

        given().contentType(ContentType.JSON).body(body)
                .when().post(BASE)
                .then().statusCode(201);

        // Ugyanaz az email → 409
        given().contentType(ContentType.JSON).body(body)
                .when().post(BASE)
                .then().statusCode(409);
    }

    @Test
    void testNemLetezoId() {
        given().when().get(BASE + "/00000000-0000-0000-0000-000000000000")
                .then().statusCode(404);
    }

    @Test
    void testHianyosAdatok() {
        // Nev nélkül → 400
        given().contentType(ContentType.JSON)
                .body(Map.of("email", "valami@test.hu", "szerep", "VEVO"))
                .when().post(BASE)
                .then().statusCode(400);
    }
}
