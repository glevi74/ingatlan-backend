package hu.ingatlan.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feladatok")
@Getter @Setter
public class Feladat extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @NotBlank
    @Column(nullable = false)
    public String cim;

    @Column(columnDefinition = "TEXT")
    public String leiras;

    @Column
    public LocalDate hatarido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public FeladatPrioritas prioritas = FeladatPrioritas.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public FeladatStatus status = FeladatStatus.NYITOTT;

    /** A feladathoz rendelt felhasználó UUID-ja (opcionális) */
    @Column(name = "hozzarendelt_id", columnDefinition = "uuid")
    public UUID hozzarendeltId;

    /** A feladatot létrehozó felhasználó UUID-ja (JWT sub-ból töltve) */
    @Column(name = "letrehozo_id", columnDefinition = "uuid")
    public UUID letrehozoId;

    /** Multitenancy szűrőkulcs */
    @Column(name = "iroda_id", nullable = false, columnDefinition = "uuid")
    public UUID irodaId;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;

    @UpdateTimestamp
    @Column(name = "modositva")
    public LocalDateTime modositva;

    // ── Opcionális entitás-hivatkozás ─────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "kapcsolt_tipus")
    public KapcsoltTipus kapcsoltTipus;

    @Column(name = "kapcsolt_id", columnDefinition = "uuid")
    public UUID kapcsoltId;

    /** Gyorsítótárazott megjelenítési név */
    @Column(name = "kapcsolt_nev")
    public String kapcsoltNev;

    public enum FeladatStatus {
        NYITOTT, FOLYAMATBAN, KESZ, MEGHIUSULT
    }

    public enum FeladatPrioritas {
        ALACSONY, NORMAL, MAGAS, SURGOS
    }

    public enum KapcsoltTipus {
        INGATLAN, UGYFEL, MEGBIZAS
    }
}
