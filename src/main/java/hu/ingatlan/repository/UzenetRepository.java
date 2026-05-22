package hu.ingatlan.repository;

import hu.ingatlan.domain.Uzenet;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UzenetRepository implements PanacheRepositoryBase<Uzenet, UUID> {

    private static final Sort BY_LETREHOZVA = Sort.ascending("letrehozva");

    /** Csatorna összes üzenetét legrégebbitől rendezi */
    public List<Uzenet> findByCsatorna(UUID csatornaId) {
        return list("csatornaId", BY_LETREHOZVA, csatornaId);
    }

    /** Csak a megadott időpont utáni üzenetek (polling) */
    public List<Uzenet> findByCsatornaSince(UUID csatornaId, LocalDateTime since) {
        return list("csatornaId = :c AND letrehozva > :s", BY_LETREHOZVA,
                Parameters.with("c", csatornaId).and("s", since));
    }

    public long countByCsatorna(UUID csatornaId) {
        return count("csatornaId", csatornaId);
    }

    /** Legutóbbi N üzenet (lapozás nélküli, kompakt lista) */
    public List<Uzenet> findLastByCsatorna(UUID csatornaId, int limit) {
        return find("csatornaId", Sort.descending("letrehozva"), csatornaId)
                .page(0, limit)
                .list()
                .stream()
                .sorted(java.util.Comparator.comparing(u -> u.letrehozva))
                .toList();
    }
}
