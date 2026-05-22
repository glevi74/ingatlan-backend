package hu.ingatlan.repository;

import hu.ingatlan.domain.Hirdetes;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class HirdetesRepository implements PanacheRepositoryBase<Hirdetes, UUID> {

    public List<Hirdetes> listByIroda(UUID irodaId) {
        return list("irodaId", Sort.by("letrehozva").descending(), irodaId);
    }

    public List<Hirdetes> findByMegbizasAndIroda(UUID megbizasId, UUID irodaId) {
        return list("megbizas.id = :megbizasId AND irodaId = :irodaId",
                Parameters.with("megbizasId", megbizasId).and("irodaId", irodaId));
    }

    public List<Hirdetes> findAktivakByIroda(UUID irodaId) {
        return list("irodaId = :irodaId AND status = :status",
                Sort.by("letrehozva").descending(),
                Parameters.with("irodaId", irodaId).and("status", Hirdetes.HirdetesStatus.AKTIV));
    }

    // ADMIN-variánsok: iroda-szűrés nélkül
    public List<Hirdetes> findAktivak() {
        return list("status", Sort.by("letrehozva").descending(), Hirdetes.HirdetesStatus.AKTIV);
    }

    public List<Hirdetes> findByMegbizas(UUID megbizasId) {
        return list("megbizas.id", megbizasId);
    }
}
