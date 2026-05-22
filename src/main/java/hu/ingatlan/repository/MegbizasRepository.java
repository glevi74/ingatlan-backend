package hu.ingatlan.repository;

import hu.ingatlan.domain.Megbizas;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MegbizasRepository implements PanacheRepositoryBase<Megbizas, UUID> {

    public List<Megbizas> listByIroda(UUID irodaId) {
        return list("irodaId", Sort.by("letrehozva").descending(), irodaId);
    }

    public List<Megbizas> findAktivakByIroda(UUID irodaId) {
        return list("irodaId = :irodaId AND status = :status",
                Sort.by("letrehozva").descending(),
                Parameters.with("irodaId", irodaId).and("status", Megbizas.MegbizasStatus.AKTIV));
    }

    public List<Megbizas> findByUgyfelAndIroda(UUID ugyfelId, UUID irodaId) {
        return list("ugyfel.id = :ugyfelId AND irodaId = :irodaId",
                Parameters.with("ugyfelId", ugyfelId).and("irodaId", irodaId));
    }

    public List<Megbizas> findByIngatlanAndIroda(UUID ingatlanId, UUID irodaId) {
        return list("ingatlan.id = :ingatlanId AND irodaId = :irodaId",
                Parameters.with("ingatlanId", ingatlanId).and("irodaId", irodaId));
    }

    // ADMIN-variánsok: iroda-szűrés nélkül
    public List<Megbizas> findAktivak() {
        return list("status", Sort.by("letrehozva").descending(), Megbizas.MegbizasStatus.AKTIV);
    }

    public List<Megbizas> findByUgyfel(UUID ugyfelId) {
        return list("ugyfel.id", ugyfelId);
    }

    public List<Megbizas> findByIngatlan(UUID ingatlanId) {
        return list("ingatlan.id", ingatlanId);
    }
}
