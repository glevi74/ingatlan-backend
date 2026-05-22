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
@Table(name = "hirdetesek")
@Getter @Setter
public class Hirdetes extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "megbizas_id", nullable = false)
    public Megbizas megbizas;

    @Column(name = "ker_ar", nullable = false)
    public Long kerAr;

    @Column
    public String portal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public HirdetesStatus status = HirdetesStatus.AKTIV;

    @Column(name = "indulas")
    public LocalDate indulas;

    @Column(name = "lejaras")
    public LocalDate lejaras;

    @Column(name = "megtekintesek")
    public Integer megtekintesek = 0;

    @Column(name = "iroda_id", nullable = false, columnDefinition = "uuid")
    public UUID irodaId;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;

    public enum HirdetesStatus { AKTIV, INAKTIV, LEZART, ELADVA }
}
