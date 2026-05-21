package hu.ingatlan.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestSecurity(user = "admin", roles = {"ADMIN"})
class IngatlanResourceTest {

    private static final String BASE = "/api/v1/ingatlanok";

    private Map<String, Object> ujIngatlan(String cim, String tipus) {
        return Map.of(
                "cim", cim,
                "tipus", tipus,
                "alapterulet", 75.0,
                "szobaszam", 3,
                "allapot", "JO");
    }

    @Test
    void testListaEsLekerdezes() {
        String id = given().contentType(ContentType.JSON)
                .body(ujIngatlan("Budapest V., Váci utca 1.", "LAKAS"))
                .when().post(BASE)
                .then().statusCode(201)
                .body("cim", containsString("Váci"))
                .body("tipus", equalTo("LAKAS"))
                .extract().path("id");

        given().when().get(BASE + "/" + id)
                .then().statusCode(200)
                .body("alapterulet", equalTo(75.0f))
                .body("szobaszam", equalTo(3));

        // Lista tartalmazza a létrehozottat
        given().when().get(BASE)
                .then().statusCode(200)
                .body("id", hasItem(id));
    }

    @Test
    void testSzures() {
        given().contentType(ContentType.JSON)
                .body(ujIngatlan("Szűrési Teszt Ház", "HAZ"))
                .when().post(BASE)
                .then().statusCode(201);

        given().contentType(ContentType.JSON)
                .body(ujIngatlan("Szűrési Teszt Lakás", "LAKAS"))
                .when().post(BASE)
                .then().statusCode(201);

        // Csak HAZ típusú ingatlanok
        List<String> tipusok = given()
                .queryParam("tipus", "HAZ")
                .when().get(BASE)
                .then().statusCode(200)
                .extract().jsonPath().getList("tipus");

        tipusok.forEach(t -> org.junit.jupiter.api.Assertions.assertEquals("HAZ", t));
    }

    @Test
    void testAlapteruletSzures() {
        given().contentType(ContentType.JSON)
                .body(Map.of("cim", "Kis lakás", "tipus", "LAKAS", "alapterulet", 30.0, "szobaszam", 1, "allapot", "JO"))
                .when().post(BASE)
                .then().statusCode(201);

        given().contentType(ContentType.JSON)
                .body(Map.of("cim", "Nagy ház", "tipus", "HAZ", "alapterulet", 250.0, "szobaszam", 7, "allapot", "JO"))
                .when().post(BASE)
                .then().statusCode(201);

        // 50-150 m² közötti ingatlanok nem tartalmazzák a 30-ast és 250-est
        List<Float> alapteruletek = given()
                .queryParam("minAlapterulet", 50)
                .queryParam("maxAlapterulet", 150)
                .when().get(BASE)
                .then().statusCode(200)
                .extract().jsonPath().getList("alapterulet");

        alapteruletek.forEach(a -> {
            org.junit.jupiter.api.Assertions.assertTrue(a >= 50.0f && a <= 150.0f,
                    "Alapterület kívül esik a szűrési tartományon: " + a);
        });
    }

    @Test
    void testNemLetezoId() {
        given().when().get(BASE + "/00000000-0000-0000-0000-000000000000")
                .then().statusCode(404);
    }
}
