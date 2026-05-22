package hu.ingatlan.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "megbizasok")
@Getter @Setter
public class Megbizas extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ugyfel_id", nullable = false)
    public Ugyfel ugyfel;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingatlan_id", nullable = false)
    public Ingatlan ingatlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MegbizasTipus tipus;

    @Column(name = "kezdete")
    public LocalDate kezdete;

    @Column(name = "vege")
    public LocalDate vege;

    @Column(name = "jutalek_szazalek")
    public Double jutalekSzazalek;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MegbizasStatus status = MegbizasStatus.AKTIV;

    @Column(name = "iroda_id", nullable = false, columnDefinition = "uuid")
    public UUID irodaId;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;

    @OneToMany(mappedBy = "megbizas", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Hirdetes> hirdetesek = new ArrayList<>();

    @OneToMany(mappedBy = "megbizas", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Ajanlat> ajanlatok = new ArrayList<>();

    public enum MegbizasTipus { KIZAROLAGOS, NEM_KIZAROLAGOS }
    public enum MegbizasStatus { AKTIV, LEZART, FELFUGGESZTETT }
}
