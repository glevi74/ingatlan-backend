package hu.ingatlan.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "csatornak")
@Getter @Setter
public class Csatorna extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    /** Megjelenítési név (CSOPORT/KAPCSOLT esetén hasznos; ALTALANOS-nál null → "Általános") */
    @Column
    public String nev;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public CsatornaTipus tipus;

    /** Multitenancy szűrőkulcs */
    @Column(name = "iroda_id", nullable = false, columnDefinition = "uuid")
    public UUID irodaId;

    /** A csatornát létrehozó felhasználó UUID-ja */
    @Column(name = "letrehozo_id", columnDefinition = "uuid")
    public UUID letrehozoId;

    // ── Kapcsolt entitás hivatkozások (KAPCSOLT típusnál legalább egy non-null) ──

    @Column(name = "ingatlan_id", columnDefinition = "uuid")
    public UUID ingatlanId;

    @Column(name = "feladat_id", columnDefinition = "uuid")
    public UUID feladatId;

    @Column(name = "ugyfel_id", columnDefinition = "uuid")
    public UUID ugyfelId;

    @Column(name = "megbizas_id", columnDefinition = "uuid")
    public UUID megbizasId;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;

    public enum CsatornaTipus {
        /** Iroda-szintű általános csatorna – mindenki látja */
        ALTALANOS,
        /** Privát 1:1 csatorna – csak a 2 tag látja; admin nem */
        PRIVAT,
        /** Csoport csatorna – csak a tagok látják; admin nem */
        CSOPORT,
        /** Entitáshoz kötött csatorna – iroda minden tagja látja */
        KAPCSOLT
    }
}
