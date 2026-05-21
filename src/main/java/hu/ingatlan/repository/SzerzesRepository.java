package hu.ingatlan.repository;

import hu.ingatlan.domain.Szerzes;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SzerzesRepository implements PanacheRepositoryBase<Szerzes, UUID> {

    public Optional<Szerzes> findByAjanlat(UUID ajanlatId) {
        return find("ajanlat.id", ajanlatId).firstResultOptional();
    }
}
