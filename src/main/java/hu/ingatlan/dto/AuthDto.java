package hu.ingatlan.dto;

import hu.ingatlan.domain.Felhasznalo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
    }

    @Data
    public static class TokenResponse {
        public String token;
        public String felhasznalonev;
        public Felhasznalo.FelhasznaloSzerep szerep;
    }
}
