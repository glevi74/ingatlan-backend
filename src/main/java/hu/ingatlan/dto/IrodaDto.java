package hu.ingatlan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class IrodaDto {

    @Data
    public static class Request {
        @NotBlank
        public String nev;

        /** URL-barát egyedi azonosító, pl. "kovacs-ingatlan" */
        @NotBlank
        @Pattern(regexp = "^[a-z0-9-]{3,50}$",
                 message = "A slug csak kisbetűket, számokat és kötőjelet tartalmazhat (3-50 karakter).")
        public String slug;

        public String leiras;
        public String logoUrl;
        public String telefon;
        public String email;
        public String cim;
        public String weboldal;

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Érvényes hex szín szükséges, pl. #4F46E5")
        public String szinElsodleges;

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Érvényes hex szín szükséges, pl. #7C3AED")
        public String szinMasodlagos;
    }

    @Data
    public static class Response {
        public UUID id;
        public String nev;
        public String slug;
        public String leiras;
        public String logoUrl;
        public String telefon;
        public String email;
        public String cim;
        public String weboldal;
        public String szinElsodleges;
        public String szinMasodlagos;
        public boolean aktiv;
        public LocalDateTime letrehozva;
    }
}
