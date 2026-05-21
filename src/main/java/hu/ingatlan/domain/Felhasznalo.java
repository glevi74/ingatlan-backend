package hu.ingatlan.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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
    @Column(nullable = false)
    public FelhasznaloSzerep szerep;

    @Column(nullable = false)
    public boolean aktiv = true;

    @CreationTimestamp
    @Column(name = "letrehozva", updatable = false)
    public LocalDateTime letrehozva;

    public enum FelhasznaloSzerep { ADMIN, UGYNOK }
}
