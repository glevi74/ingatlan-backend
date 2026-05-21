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

    public List<Ingatlan> findByTipus(Ingatlan.IngatlanTipus tipus) {
        return list("tipus", tipus);
    }

    public List<Ingatlan> search(Ingatlan.IngatlanTipus tipus,
                                  Double minAlapterulet,
                                  Double maxAlapterulet,
                                  Integer minSzobaszam) {
        StringBuilder query = new StringBuilder("1=1");
        Parameters params = new Parameters();

        if (tipus != null) {
            query.append(" AND tipus = :tipus");
            params.and("tipus", tipus);
        }
        if (minAlapterulet != null) {
            query.append(" AND alapterulet >= :minAlapterulet");
            params.and("minAlapterulet", minAlapterulet);
        }
        if (maxAlapterulet != null) {
            query.append(" AND alapterulet <= :maxAlapterulet");
            params.and("maxAlapterulet", maxAlapterulet);
        }
        if (minSzobaszam != null) {
            query.append(" AND szobaszam >= :minSzobaszam");
            params.and("minSzobaszam", minSzobaszam);
        }

        return list(query.toString(), Sort.by("letrehozva").descending(), params);
    }
}
