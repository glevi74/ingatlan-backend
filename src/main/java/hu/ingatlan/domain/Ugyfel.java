package hu.ingatlan.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ugyfelek")
@Getter @Setter
public class Ugyfel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @NotBlank
    @Column(nullable = false)
    public String nev;

    @Email
    @Column(unique = true)
    public String email;

    @Column
    public String telefon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public UgyfelSzerep szerep;

    @Column(name = "gdpr_beleegyezes")
    public LocalDate gdprBeleegyezes;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;

    @UpdateTimestamp
    @Column(name = "modositva")
    public LocalDateTime modositva;

    @OneToMany(mappedBy = "ugyfel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Megbizas> megbizasok = new ArrayList<>();

    @OneToMany(mappedBy = "ugyfel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Ajanlat> ajanlatok = new ArrayList<>();

    public enum UgyfelSzerep {
        ELADO, VEVO, BERLETI_ADO, BERLETI_VEVO, MINDKETTO
    }
}
