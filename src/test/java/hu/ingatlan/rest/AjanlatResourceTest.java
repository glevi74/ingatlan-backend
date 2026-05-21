package hu.ingatlan.rest;

import hu.ingatlan.domain.Ingatlan;
import hu.ingatlan.domain.Megbizas;
import hu.ingatlan.domain.Ugyfel;
import hu.ingatlan.repository.AjanlatRepository;
import hu.ingatlan.repository.IngatlanRepository;
import hu.ingatlan.repository.MegbizasRepository;
import hu.ingatlan.repository.UgyfelRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestSecurity(user = "admin", roles = {"ADMIN"})
class AjanlatResourceTest {

    @Inject UgyfelRepository ugyfelRepo;
    @Inject IngatlanRepository ingatlanRepo;
    @Inject MegbizasRepository megbizasRepo;
    @Inject AjanlatRepository ajanlatRepo;

    String megbizasId;
    String ugyfelId;

    @BeforeEach
    @Transactional
    void setup() {
        Ugyfel u = new Ugyfel();
        u.nev = "Ajanlat Teszt Eladó";
        u.email = "ajanlat.teszt." + UUID.randomUUID() + "@test.hu";
        u.szerep = Ugyfel.UgyfelSzerep.ELADO;
        ugyfelRepo.persist(u);
        ugyfelId = u.id.toString();

        Ingatlan i = new Ingatlan();
        i.cim = "Ajanlat Teszt Cím";
        i.tipus = Ingatlan.IngatlanTipus.LAKAS;
        ingatlanRepo.persist(i);

        Megbizas m = new Megbizas();
        m.ugyfel = u;
        m.ingatlan = i;
        m.tipus = Megbizas.MegbizasTipus.KIZAROLAGOS;
        megbizasRepo.persist(m);
        megbizasId = m.id.toString();
    }

    @AfterEach
    @Transactional
    void teardown() {
        ajanlatRepo.deleteAll();
        megbizasRepo.deleteAll();
        ingatlanRepo.delete("cim", "Ajanlat Teszt Cím");
        ugyfelRepo.delete("nev", "Ajanlat Teszt Eladó");
    }

    @Test
    void testAjanlatLetrehozas() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "megbizasId", megbizasId,
                        "ugyfelId", ugyfelId,
                        "ajanlottAr", 45000000,
                        "datum", "2024-06-01"))
                .when().post("/api/v1/ajanlatok")
                .then().statusCode(201)
                .body("status", equalTo("BEERKEZETT"))
                .body("ajanlottAr", equalTo(45000000))
                .body("megbizasId", equalTo(megbizasId))
                .body("id", notNullValue());
    }

    @Test
    void testElfogadasiFlow() {
        // Két ajánlat ugyanarra a megbízásra
        String ajanlatId1 = given().contentType(ContentType.JSON)
                .body(Map.of("megbizasId", megbizasId, "ugyfelId", ugyfelId,
                        "ajanlottAr", 50000000, "datum", "2024-06-01"))
                .when().post("/api/v1/ajanlatok")
                .then().statusCode(201)
                .extract().path("id");

        String ajanlatId2 = given().contentType(ContentType.JSON)
                .body(Map.of("megbizasId", megbizasId, "ugyfelId", ugyfelId,
                        "ajanlottAr", 48000000, "datum", "2024-06-02"))
                .when().post("/api/v1/ajanlatok")
                .then().statusCode(201)
                .extract().path("id");

        // Az első ajánlat elfogadása
        given().when().patch("/api/v1/ajanlatok/" + ajanlatId1 + "/elfogad")
                .then().statusCode(200)
                .body("status", equalTo("ELFOGADOTT"));

        // A második ajánlat automatikusan ELUTASITOTT
        given().when().get("/api/v1/ajanlatok/" + ajanlatId2)
                .then().statusCode(200)
                .body("status", equalTo("ELUTASITOTT"));

        // A megbízás lezárult
        given().when().get("/api/v1/megbizasok/" + megbizasId)
                .then().statusCode(200)
                .body("status", equalTo("LEZART"));
    }

    @Test
    void testElfogadasKetszer() {
        String ajanlatId = given().contentType(ContentType.JSON)
                .body(Map.of("megbizasId", megbizasId, "ugyfelId", ugyfelId,
                        "ajanlottAr", 50000000, "datum", "2024-06-01"))
                .when().post("/api/v1/ajanlatok")
                .then().statusCode(201)
                .extract().path("id");

        given().when().patch("/api/v1/ajanlatok/" + ajanlatId + "/elfogad")
                .then().statusCode(200);

        // Másodszori elfogadás → 400
        given().when().patch("/api/v1/ajanlatok/" + ajanlatId + "/elfogad")
                .then().statusCode(400);
    }

    @Test
    void testElfogadasInaktivMegbizasra() {
        // Megbízás lezárása
        given().queryParam("status", "LEZART")
                .when().patch("/api/v1/megbizasok/" + megbizasId + "/status")
                .then().statusCode(200);

        String ajanlatId = given().contentType(ContentType.JSON)
                .body(Map.of("megbizasId", megbizasId, "ugyfelId", ugyfelId,
                        "ajanlottAr", 50000000, "datum", "2024-06-01"))
                .when().post("/api/v1/ajanlatok")
                .then().statusCode(201)
                .extract().path("id");

        // Lezárt megbízáshoz tartozó ajánlat nem fogadható el → 400
        given().when().patch("/api/v1/ajanlatok/" + ajanlatId + "/elfogad")
                .then().statusCode(400);
    }

    @Test
    void testNemLetezoMegbizas() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "megbizasId", "00000000-0000-0000-0000-000000000000",
                        "ugyfelId", ugyfelId,
                        "ajanlottAr", 50000000))
                .when().post("/api/v1/ajanlatok")
                .then().statusCode(404);
    }
}
