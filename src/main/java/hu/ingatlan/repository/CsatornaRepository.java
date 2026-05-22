package hu.ingatlan.repository;

import hu.ingatlan.domain.Csatorna;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CsatornaRepository implements PanacheRepositoryBase<Csatorna, UUID> {

    private static final Sort BY_LETREHOZVA = Sort.ascending("letrehozva");

    /** Az iroda összes csatornája (admin használja listázáshoz) */
    public List<Csatorna> listByIroda(UUID irodaId) {
        return list("irodaId", BY_LETREHOZVA, irodaId);
    }

    /** Adott típusú csatornák az irodában */
    public List<Csatorna> listByIrodaAndTipusok(UUID irodaId, List<Csatorna.CsatornaTipus> tipusok) {
        return list("irodaId = :i AND tipus IN :types", BY_LETREHOZVA,
                Parameters.with("i", irodaId).and("types", tipusok));
    }

    /** Csatornák lekérése UUID-k listája alapján (PRIVAT/CSOPORT tagok számára) */
    public List<Csatorna> listByIds(List<UUID> ids) {
        if (ids.isEmpty()) return List.of();
        return list("id IN :ids", BY_LETREHOZVA, Parameters.with("ids", ids));
    }

    /** Az iroda ALTALANOS csatornája – ha van */
    public Optional<Csatorna> findAltalanos(UUID irodaId) {
        return find("irodaId = :i AND tipus = :t",
                Parameters.with("i", irodaId).and("t", Csatorna.CsatornaTipus.ALTALANOS))
                .firstResultOptional();
    }

    /** KAPCSOLT csatorna keresése entitás-link alapján */
    public Optional<Csatorna> findKapcsolt(UUID irodaId,
                                           UUID ingatlanId,
                                           UUID feladatId,
                                           UUID ugyfelId,
                                           UUID megbizasId) {
        StringBuilder q = new StringBuilder("irodaId = :i AND tipus = :t");
        Parameters p = Parameters.with("i", irodaId).and("t", Csatorna.CsatornaTipus.KAPCSOLT);

        if (ingatlanId != null) { q.append(" AND ingatlanId = :ing"); p.and("ing", ingatlanId); }
        else                    { q.append(" AND ingatlanId IS NULL"); }

        if (feladatId != null)  { q.append(" AND feladatId = :fel");  p.and("fel", feladatId); }
        else                    { q.append(" AND feladatId IS NULL"); }

        if (ugyfelId != null)   { q.append(" AND ugyfelId = :ugy");   p.and("ugy", ugyfelId); }
        else                    { q.append(" AND ugyfelId IS NULL"); }

        if (megbizasId != null) { q.append(" AND megbizasId = :meg"); p.and("meg", megbizasId); }
        else                    { q.append(" AND megbizasId IS NULL"); }

        return find(q.toString(), p).firstResultOptional();
    }
}
