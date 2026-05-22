package hu.ingatlan.repository;

import hu.ingatlan.domain.Szerzes;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SzerzesRepository implements PanacheRepositoryBase<Szerzes, UUID> {

    public List<Szerzes> listByIroda(UUID irodaId) {
        return list("irodaId", Sort.by("letrehozva").descending(), irodaId);
    }

    public Optional<Szerzes> findByAjanlatAndIroda(UUID ajanlatId, UUID irodaId) {
        return find("ajanlat.id = :ajanlatId AND irodaId = :irodaId",
                Parameters.with("ajanlatId", ajanlatId).and("irodaId", irodaId))
                .firstResultOptional();
    }
}
