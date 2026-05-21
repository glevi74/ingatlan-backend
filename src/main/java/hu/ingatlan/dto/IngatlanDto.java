package hu.ingatlan.dto;

import hu.ingatlan.domain.Ingatlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class IngatlanDto {

    @Data
    public static class Request {
        @NotBlank
        public String cim;
        public String helyrajziSzam;
        @NotNull
        public Ingatlan.IngatlanTipus tipus;
        @Positive
        public Double alapterulet;
        public Double telekterulet;
        public Integer szobaszam;
        public Integer emelet;
        public Ingatlan.IngatlanAllapot allapot;
        public String energetikaiOsztaly;
        public String jogiStatus;
        public String leiras;
    }

    @Data
    public static class Response {
        public UUID id;
        public String cim;
        public String helyrajziSzam;
        public Ingatlan.IngatlanTipus tipus;
        public Double alapterulet;
        public Double telekterulet;
        public Integer szobaszam;
        public Integer emelet;
        public Ingatlan.IngatlanAllapot allapot;
        public String energetikaiOsztaly;
        public String jogiStatus;
        public String leiras;
        public LocalDateTime letrehozva;
    }

    @Data
    public static class SearchParams {
        public Ingatlan.IngatlanTipus tipus;
        public Double minAlapterulet;
        public Double maxAlapterulet;
        public Integer minSzobaszam;
    }
}
