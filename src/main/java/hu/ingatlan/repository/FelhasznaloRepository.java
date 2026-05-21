package hu.ingatlan.repository;

import hu.ingatlan.domain.Felhasznalo;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class FelhasznaloRepository implements PanacheRepositoryBase<Felhasznalo, UUID> {

    public Felhasznalo findByFelhasznalonev(String felhasznalonev) {
        return find("felhasznalonev", felhasznalonev).firstResult();
    }
}
