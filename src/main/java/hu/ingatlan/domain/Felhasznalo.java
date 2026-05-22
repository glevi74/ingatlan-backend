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
@Table(name = "felhasznalok")
@Getter @Setter
public class Felhasznalo extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @NotBlank
    @Column(unique = true, nullable = false)
    public String felhasznalonev;

    @NotBlank
    @Column(name = "jelszo_hash", nullable = false)
    public String jelszoHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    public FelhasznaloSzerep szerep;

    /** null = ADMIN (rendszer-szintű, nem tartozik irodához) */
    @Column(name = "iroda_id", columnDefinition = "uuid")
    public UUID irodaId;

    /** Megjelenítési teljes név */
    @Column
    public String nev;

    @Column
    public String email;

    @Column
    public String telefon;

    @Column(name = "profilkep_url")
    public String profilkepUrl;

    @Column(nullable = false)
    public boolean aktiv = true;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;

    @UpdateTimestamp
    @Column(name = "modositva")
    public LocalDateTime modositva;

    public enum FelhasznaloSzerep {
        /** Rendszer-szintű adminisztrátor – irodákat kezel, globális hozzáférés */
        ADMIN,
        /** Irodavezető – saját iroda adatai + felhasználók létrehozása */
        IRODAVEZETO,
        /** Értékesítési referens – saját iroda összes adata */
        REFERENS,
        /** Irodai asszisztens – saját iroda összes adata */
        ASSZISZTENS
    }
}
