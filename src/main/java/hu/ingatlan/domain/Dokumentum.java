package hu.ingatlan.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dokumentumok")
@Getter @Setter
public class Dokumentum extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingatlan_id", nullable = false)
    public Ingatlan ingatlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public DokumentumTipus tipus;

    @Column(nullable = false)
    public String fajlnev;

    @Column(name = "fajl_url")
    public String fajlUrl;

    @Column
    public LocalDate datum;

    @CreationTimestamp
    @Column(name = "feltoltve", updatable = false)
    public LocalDateTime feltoltve;

    public enum DokumentumTipus {
        TULAJDONI_LAP, ENERGETIKAI_TANUSITVANY, ALAPRAJZ,
        ADASVÉTELI_SZERZODES, BERLETI_SZERZODES, FOTO, EGYEB
    }
}
