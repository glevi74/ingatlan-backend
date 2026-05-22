package hu.ingatlan.repository;

import hu.ingatlan.domain.Ingatlan;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class IngatlanRepository implements PanacheRepositoryBase<Ingatlan, UUID> {

    public List<Ingatlan> listByIroda(UUID irodaId) {
        return list("irodaId", Sort.by("letrehozva").descending(), irodaId);
    }

    public List<Ingatlan> search(UUID irodaId,
                                  Ingatlan.IngatlanTipus tipus,
                                  Double minAlapterulet,
                                  Double maxAlapterulet,
                                  Integer minSzobaszam) {
        StringBuilder query = new StringBuilder();
        Parameters params = new Parameters();

        if (irodaId != null) {
            query.append("irodaId = :irodaId");
            params.and("irodaId", irodaId);
        }
        if (tipus != null) {
            if (!query.isEmpty()) query.append(" AND ");
            query.append("tipus = :tipus");
            params.and("tipus", tipus);
        }
        if (minAlapterulet != null) {
            if (!query.isEmpty()) query.append(" AND ");
            query.append("alapterulet >= :minAlapterulet");
            params.and("minAlapterulet", minAlapterulet);
        }
        if (maxAlapterulet != null) {
            if (!query.isEmpty()) query.append(" AND ");
            query.append("alapterulet <= :maxAlapterulet");
            params.and("maxAlapterulet", maxAlapterulet);
        }
        if (minSzobaszam != null) {
            if (!query.isEmpty()) query.append(" AND ");
            query.append("szobaszam >= :minSzobaszam");
            params.and("minSzobaszam", minSzobaszam);
        }

        String q = query.isEmpty() ? "1=1" : query.toString();
        return list(q, Sort.by("letrehozva").descending(), params);
    }
}
