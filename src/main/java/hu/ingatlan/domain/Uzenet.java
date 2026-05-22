package hu.ingatlan.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "uzenetek")
@Getter @Setter
public class Uzenet extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @Column(name = "csatorna_id", nullable = false, columnDefinition = "uuid")
    public UUID csatornaId;

    @Column(name = "felado_id", nullable = false, columnDefinition = "uuid")
    public UUID feladoId;

    /** Üzenet szövege – null lehet, ha csak kapcsolt elem van */
    @Column(columnDefinition = "TEXT")
    public String szoveg;

    /** Denormalizált – a csatorna iroda_id-ja; gyors szűréshez */
    @Column(name = "iroda_id", nullable = false, columnDefinition = "uuid")
    public UUID irodaId;

    // ── Opcionális entitás-hivatkozás (Ingatlan / Ügyfél / Megbízás) ──────

    @Enumerated(EnumType.STRING)
    @Column(name = "kapcsolt_tipus")
    public KapcsoltTipus kapcsoltTipus;

    @Column(name = "kapcsolt_id", columnDefinition = "uuid")
    public UUID kapcsoltId;

    /** Gyorsítótárazott megjelenítési név – felesleges join nélküli rendereléshez */
    @Column(name = "kapcsolt_nev")
    public String kapcsoltNev;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;

    public enum KapcsoltTipus {
        INGATLAN, UGYFEL, MEGBIZAS, FELADAT
    }
}
