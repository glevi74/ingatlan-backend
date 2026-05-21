package hu.ingatlan.dto;

import hu.ingatlan.domain.Szerzes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class SzerzesDto {

    @Data
    public static class Request {
        @NotNull
        public UUID ajanlatId;
        @NotNull
        @Positive
        public Long vegsoAr;
        public LocalDate szerzodesDatum;
        public Long jutalekOsszeg;
        public String megjegyzes;
    }

    @Data
    public static class Response {
        public UUID id;
        public UUID ajanlatId;
        public String ingatlanCim;
        public UUID ugyfelId;
        public String ugyfelNev;
        public Long vegsoAr;
        public LocalDate szerzodesDatum;
        public Long jutalekOsszeg;
        public Szerzes.SzerzesStatus status;
        public String megjegyzes;
        public LocalDateTime letrehozva;
    }
}
