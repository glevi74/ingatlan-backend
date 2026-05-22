package hu.ingatlan.repository;

import hu.ingatlan.domain.CsatornaTags;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CsatornatagsRepository implements PanacheRepositoryBase<CsatornaTags, UUID> {

    public List<CsatornaTags> findByCsatorna(UUID csatornaId) {
        return list("csatornaId", csatornaId);
    }

    /** Az összes csatorna-ID, amelynek a felhasználó tagja */
    public List<CsatornaTags> findByFelhasznalo(UUID felhasznaloId) {
        return list("felhasznaloId", felhasznaloId);
    }

    public boolean isTags(UUID csatornaId, UUID felhasznaloId) {
        return count("csatornaId = :c AND felhasznaloId = :f",
                Parameters.with("c", csatornaId).and("f", felhasznaloId)) > 0;
    }

    public void deleteAllByCsatorna(UUID csatornaId) {
        delete("csatornaId", csatornaId);
    }
}
