package hu.ingatlan.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class HirDto {

    @Data
    public static class Response {
        public UUID id;
        public LocalDate datum;
        public String tartalom;
        public LocalDateTime letrehozva;
    }

    @Data
    public static class GeneralasRequest {
        /** A dátum, amelyre az összefoglalót generálni kell (alapértelmezés: tegnap) */
        public LocalDate datum;
    }
}
