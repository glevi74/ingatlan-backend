package hu.ingatlan.dto;

import hu.ingatlan.domain.Ajanlat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class AjanlatDto {

    @Data
    public static class Request {
        @NotNull
        public UUID megbizasId;
        @NotNull
        public UUID ugyfelId;
        @NotNull
        @Positive
        public Long ajanlottAr;
        public LocalDate datum;
        public String megjegyzes;
    }

    @Data
    public static class Response {
        public UUID id;
        public UUID megbizasId;
        public String ingatlanCim;
        public UUID ugyfelId;
        public String ugyfelNev;
        public Long ajanlottAr;
        public LocalDate datum;
        public Ajanlat.AjanlatStatus status;
        public String megjegyzes;
        public UUID szerzesId;
        public LocalDateTime letrehozva;
    }
}
