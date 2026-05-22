package hu.ingatlan.dto;

import hu.ingatlan.domain.Ingatlan;
import hu.ingatlan.domain.Ugyfel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// ===== UGYFEL =====

public class UgyfelDto {

    @Data
    public static class Request {
        @NotBlank
        public String nev;
        @Email
        public String email;
        public String telefon;
        @NotNull
        public Ugyfel.UgyfelSzerep szerep;
        public LocalDate gdprBeleegyezes;
    }

    @Data
    public static class Response {
        public UUID id;
        public UUID irodaId;
        public String nev;
        public String email;
        public String telefon;
        public Ugyfel.UgyfelSzerep szerep;
        public LocalDate gdprBeleegyezes;
        public LocalDateTime letrehozva;
    }
}
