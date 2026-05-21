# Ingatlanközvetítő Backend

Quarkus 3 alapú REST API ingatlanközvetítő szoftverhez.

## Technológiák

- **Java 21**
- **Quarkus 3.9** (quarkus-rest, hibernate-orm-panache, smallrye-openapi)
- **PostgreSQL 16**
- **Lombok** + **MapStruct**
- **Maven 3.9**

## Projekt struktúra

```
src/main/java/hu/ingatlan/
├── domain/          JPA entitások (Ugyfel, Ingatlan, Megbizas, ...)
├── repository/      Panache repository-k keresési metódusokkal
├── service/         Üzleti logika, tranzakciók
├── rest/            JAX-RS REST endpointok
└── dto/             Request/Response DTO-k
```

## Indítás

### 1. Adatbázis indítása

```bash
docker-compose up -d
```

### 2. Quarkus dev mód

```bash
./mvnw quarkus:dev
```

### 3. Swagger UI

```
http://localhost:8080/swagger-ui
```

### 4. OpenAPI spec

```
http://localhost:8080/openapi
```

## API végpontok

| Metódus | Útvonal                        | Leírás                     |
|---------|-------------------------------|----------------------------|
| GET     | /api/v1/ugyfelek              | Összes ügyfél              |
| GET     | /api/v1/ugyfelek/{id}         | Ügyfél lekérdezése         |
| POST    | /api/v1/ugyfelek              | Új ügyfél                  |
| PUT     | /api/v1/ugyfelek/{id}         | Ügyfél módosítása          |
| DELETE  | /api/v1/ugyfelek/{id}         | Ügyfél törlése             |
| GET     | /api/v1/ingatlanok            | Ingatlanok (+ keresés)     |
| GET     | /api/v1/ingatlanok/{id}       | Ingatlan lekérdezése       |
| POST    | /api/v1/ingatlanok            | Új ingatlan                |
| PUT     | /api/v1/ingatlanok/{id}       | Ingatlan módosítása        |
| DELETE  | /api/v1/ingatlanok/{id}       | Ingatlan törlése           |
| GET     | /api/v1/megbizasok            | Megbízások (+ szűrés)      |
| GET     | /api/v1/megbizasok/{id}       | Megbízás lekérdezése       |
| POST    | /api/v1/megbizasok            | Új megbízás                |
| PUT     | /api/v1/megbizasok/{id}       | Megbízás módosítása        |
| PATCH   | /api/v1/megbizasok/{id}/status| Státusz módosítása         |
| DELETE  | /api/v1/megbizasok/{id}       | Megbízás törlése           |
| GET     | /api/v1/hirdetesek            | Hirdetések (+ szűrés)      |
| GET     | /api/v1/hirdetesek/{id}       | Hirdetés lekérdezése       |
| POST    | /api/v1/hirdetesek            | Új hirdetés                |
| PUT     | /api/v1/hirdetesek/{id}       | Hirdetés módosítása        |
| PATCH   | /api/v1/hirdetesek/{id}/status| Státusz módosítása         |
| POST    | /api/v1/hirdetesek/{id}/megtekintes | Megtekintés növelése |
| DELETE  | /api/v1/hirdetesek/{id}       | Hirdetés törlése           |
| GET     | /api/v1/ajanlatok             | Ajánlatok (+ szűrés)       |
| GET     | /api/v1/ajanlatok/{id}        | Ajánlat lekérdezése        |
| POST    | /api/v1/ajanlatok             | Új ajánlat                 |
| PUT     | /api/v1/ajanlatok/{id}        | Ajánlat módosítása         |
| PATCH   | /api/v1/ajanlatok/{id}/status | Státusz módosítása         |
| PATCH   | /api/v1/ajanlatok/{id}/elfogad| Ajánlat elfogadása         |
| DELETE  | /api/v1/ajanlatok/{id}        | Ajánlat törlése            |
| GET     | /api/v1/szerzesek             | Szerzések (+ szűrés)       |
| GET     | /api/v1/szerzesek/{id}        | Szerzés lekérdezése        |
| POST    | /api/v1/szerzesek             | Szerzés rögzítése          |
| PUT     | /api/v1/szerzesek/{id}        | Szerzés módosítása         |
| PATCH   | /api/v1/szerzesek/{id}/status | Státusz módosítása         |
| DELETE  | /api/v1/szerzesek/{id}        | Szerzés törlése            |

### Ingatlan keresés query paraméterekkel

```
GET /api/v1/ingatlanok?tipus=LAKAS&minAlapterulet=50&maxAlapterulet=100&minSzobaszam=2
```

### Megbízás szűrés query paraméterekkel

```
GET /api/v1/megbizasok?ugyfelId=<uuid>
GET /api/v1/megbizasok?ingatlanId=<uuid>
GET /api/v1/megbizasok?aktivOnly=true
PATCH /api/v1/megbizasok/{id}/status?status=LEZART
```

## Autentikáció

### Kulcsgenerálás (első indítás előtt)

```bash
bash generate-keys.sh
```

Ez létrehozza a `src/main/resources/privateKey.pem` és `publicKey.pem` fájlokat.
**A `privateKey.pem`-et soha ne commitold!**

### Bejelentkezés

```
POST /api/v1/auth/bejelentkezes
{ "felhasznalonev": "admin", "jelszo": "admin123" }
```

Az első indításkor automatikusan létrejön az `admin` / `admin123` felhasználó (lásd a logot).
Éles környezetben az első lépés az alapértelmezett jelszó cseréje.

### Token használata

```
Authorization: Bearer <token>
```

A Swagger UI-ban az "Authorize" gombbal adható meg a token.

### Szerepkörök

| Szerep  | Jogosultság                          |
|---------|--------------------------------------|
| ADMIN   | Minden végpont + regisztrálás        |
| UGYNOK  | Összes API végpont (regisztrálás nélkül) |

## Tesztek futtatása

```bash
./mvnw test
```

A tesztek H2 in-memory adatbázist használnak, PostgreSQL nem szükséges.
`@TestSecurity` gondoskodik a JWT bypass-ról — a tesztekhez nem kellenek JWT kulcsok.

| Tesztosztály | Mit fed le |
|---|---|
| `UgyfelResourceTest` | CRUD, duplikált email, validáció |
| `IngatlanResourceTest` | CRUD, típus szerinti szűrés, alapterület szűrés |
| `AjanlatResourceTest` | Létrehozás, elfogadási flow, lezárt megbízás |
| `AuthResourceTest` | Hibás bejelentkezés, jogosultság ellenőrzés |

## Health check

```
http://localhost:8080/q/health
```

## Következő lépések

- [x] Megbízás REST endpoint
- [x] Hirdetés REST endpoint
- [x] Ajánlat + Szerzés REST endpoint
- [x] JWT alapú autentikáció (quarkus-smallrye-jwt)
- [ ] Fájlfeltöltés dokumentumokhoz (quarkus-reactive-routes + S3)
- [x] Tesztek (QuarkusTest + RestAssured)
