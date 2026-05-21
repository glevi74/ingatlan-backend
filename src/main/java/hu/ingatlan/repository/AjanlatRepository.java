package hu.ingatlan.repository;

import hu.ingatlan.domain.Ajanlat;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AjanlatRepository implements PanacheRepositoryBase<Ajanlat, UUID> {

    public List<Ajanlat> findByMegbizas(UUID megbizasId) {
        return list("megbizas.id", megbizasId);
    }

    public List<Ajanlat> findByUgyfel(UUID ugyfelId) {
        return list("ugyfel.id", ugyfelId);
    }
}
