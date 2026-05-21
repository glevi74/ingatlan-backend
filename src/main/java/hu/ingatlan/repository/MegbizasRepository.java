package hu.ingatlan.repository;

import hu.ingatlan.domain.Megbizas;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MegbizasRepository implements PanacheRepositoryBase<Megbizas, UUID> {

    public List<Megbizas> findAktivak() {
        return list("status", Megbizas.MegbizasStatus.AKTIV);
    }

    public List<Megbizas> findByUgyfel(UUID ugyfelId) {
        return list("ugyfel.id", ugyfelId);
    }

    public List<Megbizas> findByIngatlan(UUID ingatlanId) {
        return list("ingatlan.id", ingatlanId);
    }
}
