package hu.ingatlan.repository;

import hu.ingatlan.domain.Iroda;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class IrodaRepository implements PanacheRepositoryBase<Iroda, UUID> {

    public Optional<Iroda> findBySlug(String slug) {
        return find("slug", slug).firstResultOptional();
    }

    public boolean slugFoglalt(String slug, UUID kizartId) {
        if (kizartId == null) return find("slug", slug).count() > 0;
        return find("slug = :slug AND id != :id",
                io.quarkus.panache.common.Parameters.with("slug", slug).and("id", kizartId)).count() > 0;
    }
}
