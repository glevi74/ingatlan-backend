package hu.ingatlan.dto;

import hu.ingatlan.domain.Feladat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class FeladatDto {

    @Data
    public static class Request {
        @NotBlank
        public String cim;

        public String leiras;
        public LocalDate hatarido;

        @NotNull
        public Feladat.FeladatPrioritas prioritas;

        /** A feladathoz rendelt felhasználó UUID-ja (opcionális) */
        public UUID hozzarendeltId;

        /** ADMIN felhasználó esetén kötelező: melyik iroda feladataként kell létrehozni */
        public UUID irodaId;
    }

    @Data
    public static class Response {
        public UUID id;
        public String cim;
        public String leiras;
        public LocalDate hatarido;
        public Feladat.FeladatPrioritas prioritas;
        public Feladat.FeladatStatus status;

        public UUID hozzarendeltId;
        public String hozzarendeltNev;

        public UUID letrehozoId;
        public String letrehozoNev;

        public UUID irodaId;
        public LocalDateTime letrehozva;
        public LocalDateTime modositva;
    }
}
