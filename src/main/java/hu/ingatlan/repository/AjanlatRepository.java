package hu.ingatlan.repository;

import hu.ingatlan.domain.Ajanlat;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AjanlatRepository implements PanacheRepositoryBase<Ajanlat, UUID> {

    public List<Ajanlat> listByIroda(UUID irodaId) {
        return list("irodaId", Sort.by("letrehozva").descending(), irodaId);
    }

    public List<Ajanlat> findByMegbizasAndIroda(UUID megbizasId, UUID irodaId) {
        return list("megbizas.id = :megbizasId AND irodaId = :irodaId",
                Parameters.with("megbizasId", megbizasId).and("irodaId", irodaId));
    }

    public List<Ajanlat> findByUgyfelAndIroda(UUID ugyfelId, UUID irodaId) {
        return list("ugyfel.id = :ugyfelId AND irodaId = :irodaId",
                Parameters.with("ugyfelId", ugyfelId).and("irodaId", irodaId));
    }

    // ADMIN-variánsok: iroda-szűrés nélkül
    public List<Ajanlat> findByMegbizas(UUID megbizasId) {
        return list("megbizas.id", megbizasId);
    }

    public List<Ajanlat> findByUgyfel(UUID ugyfelId) {
        return list("ugyfel.id", ugyfelId);
    }
}
