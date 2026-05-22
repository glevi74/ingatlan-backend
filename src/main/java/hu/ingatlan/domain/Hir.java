package hu.ingatlan.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hirek")
public class Hir extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    /** Melyik napra szól az összefoglaló (egyedi) */
    @Column(nullable = false, unique = true)
    public LocalDate datum;

    /** A generált hírsummary szövege */
    @Column(columnDefinition = "TEXT", nullable = false)
    public String tartalom;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;
}
