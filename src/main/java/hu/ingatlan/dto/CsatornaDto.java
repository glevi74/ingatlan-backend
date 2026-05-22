package hu.ingatlan.dto;

import hu.ingatlan.domain.Csatorna;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CsatornaDto {

    // ── Csatorna kérések ───────────────────────────────────────────────────

    @Data
    public static class CreateRequest {
        @NotNull
        public Csatorna.CsatornaTipus tipus;

        /** Megjelenítési név – CSOPORT esetén ajánlott, egyébként opcionális */
        public String nev;

        /** Tagok UUID-listája – PRIVAT esetén pontosan 1 db, CSOPORT esetén 1+ db kell */
        public List<UUID> tagIds;

        // Kapcsolt entitás linkel (KAPCSOLT típusnál legalább egy non-null)
        public UUID ingatlanId;
        public UUID feladatId;
        public UUID ugyfelId;
        public UUID megbizasId;
    }

    // ── Csatorna válasz ────────────────────────────────────────────────────

    @Data
    public static class Response {
        public UUID id;
        public String nev;
        public Csatorna.CsatornaTipus tipus;
        public UUID irodaId;
        public UUID letrehozoId;
        public LocalDateTime letrehozva;

        // Kapcsolt entitás
        public UUID ingatlanId;
        public UUID feladatId;
        public UUID ugyfelId;
        public UUID megbizasId;

        /** Tagok listája – csak PRIVAT/CSOPORT esetén töltjük */
        public List<TagResponse> tagok;

        /** Üzenetek száma (badge megjelenítéséhez) */
        public long uzenetSzam;

        /** Legutóbbi üzenet szövege (előnézet) */
        public String utolsoUzenetSzoveg;
        public LocalDateTime utolsoUzenetIdeje;
    }

    @Data
    public static class TagResponse {
        public UUID id;
        public String nev;
    }

    // ── Üzenet kérések / válaszok ──────────────────────────────────────────

    @Data
    public static class UzenetRequest {
        /** Üzenet szövege – elhagyható, ha kapcsoltId meg van adva */
        public String szoveg;

        /** Opcionális entitás-hivatkozás */
        public hu.ingatlan.domain.Uzenet.KapcsoltTipus kapcsoltTipus;
        public UUID kapcsoltId;
        /** Gyorsítótárazott megjelenítési név (kliens adja meg) */
        public String kapcsoltNev;
    }

    @Data
    public static class UzenetResponse {
        public UUID id;
        public UUID csatornaId;
        public UUID feladoId;
        public String feladoNev;
        public String szoveg;
        public LocalDateTime letrehozva;

        public hu.ingatlan.domain.Uzenet.KapcsoltTipus kapcsoltTipus;
        public UUID kapcsoltId;
        public String kapcsoltNev;
    }
}
