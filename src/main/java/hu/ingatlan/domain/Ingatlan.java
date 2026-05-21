package hu.ingatlan.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ingatlanok")
@Getter @Setter
public class Ingatlan extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @NotBlank
    @Column(nullable = false)
    public String cim;

    @Column(name = "helyrajzi_szam", unique = true)
    public String helyrajziSzam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public IngatlanTipus tipus;

    @Positive
    @Column(name = "alapterulet")
    public Double alapterulet;

    @Column(name = "telekterulet")
    public Double telekterulet;

    @Column(name = "szobaszam")
    public Integer szobaszam;

    @Column
    public Integer emelet;

    @Enumerated(EnumType.STRING)
    @Column
    public IngatlanAllapot allapot;

    @Column(name = "energetikai_osztaly")
    public String energetikaiOsztaly;

    @Column(name = "jogi_status")
    public String jogiStatus;

    @Column(name = "leiras", columnDefinition = "TEXT")
    public String leiras;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;

    @UpdateTimestamp
    @Column(name = "modositva")
    public LocalDateTime modositva;

    @OneToMany(mappedBy = "ingatlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Megbizas> megbizasok = new ArrayList<>();

    @OneToMany(mappedBy = "ingatlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Dokumentum> dokumentumok = new ArrayList<>();

    public enum IngatlanTipus {
        LAKAS, HAZ, TELEK, IRODA, UZLETHELYISEG, GARDZS, NYARALO, EGYEB
    }

    public enum IngatlanAllapot {
        UJ_EPITES, FELUJITOTT, JO, FELUJITANDO, BONTAS_ALATT
    }
}
