package hu.ingatlan.dto;

import hu.ingatlan.domain.Hirdetes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class HirdetesDto {

    @Data
    public static class Request {
        @NotNull
        public UUID megbizasId;
        @NotNull
        @Positive
        public Long kerAr;
        public String portal;
        public LocalDate indulas;
        public LocalDate lejaras;
    }

    @Data
    public static class Response {
        public UUID id;
        public UUID irodaId;
        public UUID megbizasId;
        public String ingatlanCim;
        public Long kerAr;
        public String portal;
        public Hirdetes.HirdetesStatus status;
        public LocalDate indulas;
        public LocalDate lejaras;
        public Integer megtekintesek;
        public LocalDateTime letrehozva;
    }
}
