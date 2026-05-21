package hu.ingatlan.repository;

import hu.ingatlan.domain.*;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UgyfelRepository implements PanacheRepositoryBase<Ugyfel, UUID> {

    public List<Ugyfel> findBySzerep(Ugyfel.UgyfelSzerep szerep) {
        return list("szerep", szerep);
    }

    public Ugyfel findByEmail(String email) {
        return find("email", email).firstResult();
    }
}
