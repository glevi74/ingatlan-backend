package hu.ingatlan.repository;

import hu.ingatlan.domain.Hirdetes;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class HirdetesRepository implements PanacheRepositoryBase<Hirdetes, UUID> {

    public List<Hirdetes> findByMegbizas(UUID megbizasId) {
        return list("megbizas.id", megbizasId);
    }

    public List<Hirdetes> findAktivak() {
        return list("status", Hirdetes.HirdetesStatus.AKTIV);
    }
}
