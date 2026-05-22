package hu.ingatlan.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "irodak")
@Getter @Setter
public class Iroda extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @NotBlank
    @Column(nullable = false)
    public String nev;

    @Column(unique = true, nullable = false)
    public String slug;

    @Column(columnDefinition = "TEXT")
    public String leiras;

    @Column(name = "logo_url")
    public String logoUrl;

    @Column
    public String telefon;

    @Column
    public String email;

    @Column
    public String cim;

    @Column
    public String weboldal;

    /** CSS hex szín pl. "#4F46E5" */
    @Column(name = "szin_elsodleges", length = 7)
    public String szinElsodleges = "#4F46E5";

    @Column(name = "szin_masodlagos", length = 7)
    public String szinMasodlagos = "#7C3AED";

    @Column(nullable = false)
    public boolean aktiv = true;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;

    @UpdateTimestamp
    @Column(name = "modositva")
    public LocalDateTime modositva;
}
