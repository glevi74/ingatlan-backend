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
@Table(name = "szerzesek")
@Getter @Setter
public class Szerzes extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ajanlat_id", nullable = false, unique = true)
    public Ajanlat ajanlat;

    @Column(name = "vegso_ar", nullable = false)
    public Long vegsoAr;

    @Column(name = "szerzodes_datum")
    public LocalDate szerzodesDatum;

    @Column(name = "jutalek_osszeg")
    public Long jutalekOsszeg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public SzerzesStatus status = SzerzesStatus.FOLYAMATBAN;

    @Column(columnDefinition = "TEXT")
    public String megjegyzes;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;

    public enum SzerzesStatus { FOLYAMATBAN, LEZART, MEGHIUSULT }
}
