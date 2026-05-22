package hu.ingatlan.dto;

import hu.ingatlan.domain.Ajanlat;
import hu.ingatlan.domain.Megbizas;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class MegbizasDto {

    @Data
    public static class Request {
        @NotNull
        public UUID ugyfelId;
        @NotNull
        public UUID ingatlanId;
        @NotNull
        public Megbizas.MegbizasTipus tipus;
        public LocalDate kezdete;
        public LocalDate vege;
        @Positive
        public Double jutalekSzazalek;
    }

    @Data
    public static class Response {
        public UUID id;
        public UUID irodaId;
        public UUID ugyfelId;
        public String ugyfelNev;
        public UUID ingatlanId;
        public String ingatlanCim;
        public Megbizas.MegbizasTipus tipus;
        public LocalDate kezdete;
        public LocalDate vege;
        public Double jutalekSzazalek;
        public Megbizas.MegbizasStatus status;
        public LocalDateTime letrehozva;
    }
}
