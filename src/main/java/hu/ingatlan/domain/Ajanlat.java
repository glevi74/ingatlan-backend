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
@Table(name = "ajanlatok")
@Getter @Setter
public class Ajanlat extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "megbizas_id", nullable = false)
    public Megbizas megbizas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ugyfel_id", nullable = false)
    public Ugyfel ugyfel;

    @Column(name = "ajanlott_ar", nullable = false)
    public Long ajanlottAr;

    @Column(name = "datum")
    public LocalDate datum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AjanlatStatus status = AjanlatStatus.BEERKEZETT;

    @Column(columnDefinition = "TEXT")
    public String megjegyzes;

    @Column(name = "iroda_id", nullable = false, columnDefinition = "uuid")
    public UUID irodaId;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;

    @OneToOne(mappedBy = "ajanlat", cascade = CascadeType.ALL)
    public Szerzes szerzes;

    public enum AjanlatStatus {
        BEERKEZETT, TARGYALASBAN, ELFOGADOTT, ELUTASITOTT, VISSZAVONT
    }
}
