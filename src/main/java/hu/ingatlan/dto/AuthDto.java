package hu.ingatlan.dto;

import hu.ingatlan.domain.Felhasznalo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

public class AuthDto {

    @Data
    public static class BejelentkezesRequest {
        @NotBlank
        public String felhasznalonev;
        @NotBlank
        public String jelszo;
    }

    @Data
    public static class RegisztracioRequest {
        @NotBlank
        @Size(min = 3, max = 50)
        public String felhasznalonev;

        @NotBlank
        @Size(min = 8)
        public String jelszo;

        @NotNull
        public Felhasznalo.FelhasznaloSzerep szerep;

        /** Kötelező IRODAVEZETO/REFERENS/ASSZISZTENS esetén; ADMIN létrehozásakor elhagyható. */
        public UUID irodaId;

        public String nev;
        public String email;
        public String telefon;
    }

    @Data
    public static class TokenResponse {
        public String token;
        public String felhasznalonev;
        public String nev;
        public Felhasznalo.FelhasznaloSzerep szerep;
        /** null ADMIN esetén */
        public UUID irodaId;
        /** Az iroda neve (null ADMIN esetén) */
        public String irodaNev;
        /** Az iroda elsődleges színe (null ADMIN esetén) */
        public String irodaSzinElsodleges;
        /** Az iroda másodlagos színe (null ADMIN esetén) */
        public String irodaSzinMasodlagos;
    }
}
