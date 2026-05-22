package hu.ingatlan.repository;

import hu.ingatlan.domain.Hir;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class HirRepository implements PanacheRepositoryBase<Hir, UUID> {

    public Optional<Hir> findByDatum(LocalDate datum) {
        return find("datum", datum).firstResultOptional();
    }

    public List<Hir> findRecent(int limit) {
        return findAll(Sort.descending("datum")).page(0, limit).list();
    }

    public boolean existsByDatum(LocalDate datum) {
        return count("datum", datum) > 0;
    }
}
