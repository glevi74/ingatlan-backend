package hu.ingatlan.dto;

import hu.ingatlan.domain.Felhasznalo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class FelhasznaloDto {

    @Data
    public static class Response {
        public UUID id;
        public String felhasznalonev;
        public String nev;
        public String email;
        public String telefon;
        public Felhasznalo.FelhasznaloSzerep szerep;
        /** null = ADMIN */
        public UUID irodaId;
        /** Denormalizált iroda-név (lekérdezésnél töltve) */
        public String irodaNev;
        public boolean aktiv;
        public LocalDateTime letrehozva;
    }

    @Data
    public static class UpdateRequest {
        public String nev;
        public String email;
        public String telefon;
        @NotNull
        public Felhasznalo.FelhasznaloSzerep szerep;
    }
}
