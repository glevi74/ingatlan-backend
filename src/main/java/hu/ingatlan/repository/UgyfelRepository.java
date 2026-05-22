package hu.ingatlan.repository;

import hu.ingatlan.domain.Ugyfel;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UgyfelRepository implements PanacheRepositoryBase<Ugyfel, UUID> {

    public List<Ugyfel> listByIroda(UUID irodaId) {
        return list("irodaId", Sort.by("letrehozva").descending(), irodaId);
    }

    public Ugyfel findByEmailAndIroda(String email, UUID irodaId) {
        return find("email = :email AND irodaId = :irodaId",
                io.quarkus.panache.common.Parameters.with("email", email).and("irodaId", irodaId))
                .firstResult();
    }
}
