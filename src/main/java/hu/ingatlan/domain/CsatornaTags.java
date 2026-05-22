package hu.ingatlan.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * PRIVAT és CSOPORT csatornák tagjainak nyilvántartása.
 * ALTALANOS és KAPCSOLT csatornáknál nem releváns (azokat az iroda mindenki látja).
 */
@Entity
@Table(
    name = "csatorna_tagok",
    uniqueConstraints = @UniqueConstraint(columnNames = {"csatorna_id", "felhasznalo_id"})
)
@Getter @Setter
public class CsatornaTags extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @Column(name = "csatorna_id", nullable = false, columnDefinition = "uuid")
    public UUID csatornaId;

    @Column(name = "felhasznalo_id", nullable = false, columnDefinition = "uuid")
    public UUID felhasznaloId;
}
