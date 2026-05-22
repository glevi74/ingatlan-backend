package hu.ingatlan.repository;

import hu.ingatlan.domain.Felhasznalo;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FelhasznaloRepository implements PanacheRepositoryBase<Felhasznalo, UUID> {

    public Felhasznalo findByFelhasznalonev(String felhasznalonev) {
        return find("felhasznalonev", felhasznalonev).firstResult();
    }

    public List<Felhasznalo> listByIroda(UUID irodaId) {
        return list("irodaId", Sort.by("letrehozva").descending(), irodaId);
    }
}
