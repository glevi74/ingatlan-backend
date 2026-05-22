package hu.ingatlan.repository;

import hu.ingatlan.domain.Feladat;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FeladatRepository implements PanacheRepositoryBase<Feladat, UUID> {

    private static final Sort BY_HATARIDO = Sort
            .ascending("hatarido")          // legközelebbi határidő elöl (null utoljára)
            .and("letrehozva", Sort.Direction.Descending);

    public List<Feladat> listByIroda(UUID irodaId) {
        return list("irodaId", BY_HATARIDO, irodaId);
    }

    public List<Feladat> listByIrodaAndStatus(UUID irodaId, Feladat.FeladatStatus status) {
        return list("irodaId = :i AND status = :s", BY_HATARIDO,
                Parameters.with("i", irodaId).and("s", status));
    }

    public List<Feladat> listByIrodaAndHozzarendelt(UUID irodaId, UUID hozzarendeltId) {
        return list("irodaId = :i AND hozzarendeltId = :h", BY_HATARIDO,
                Parameters.with("i", irodaId).and("h", hozzarendeltId));
    }
}
