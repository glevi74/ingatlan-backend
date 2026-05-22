package hu.ingatlan.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hu.ingatlan.domain.Felhasznalo;
import hu.ingatlan.domain.Iroda;
import hu.ingatlan.repository.FelhasznaloRepository;
import hu.ingatlan.repository.IrodaRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DataInitializer {

    private static final Logger LOG = Logger.getLogger(DataInitializer.class);

    @Inject FelhasznaloRepository felhasznaloRepository;
    @Inject IrodaRepository irodaRepository;
    @Inject EntityManager em;

    @Transactional
    public void onStart(@Observes StartupEvent ev) {
        fixSzerepConstraint();
        fixHelyrajziSzamEmpty();
        initIroda();
        initAdmin();
    }

    /**
     * A Hibernate "update" módban nem frissíti az enum CHECK constraint-eket.
     * Ha a szerep enum értékei változtak (pl. UGYNOK → REFERENS/ASSZISZTENS),
     * az elavult constraint törlése szükséges, különben az insert 500-as hibával fail-el.
     */
    private void fixSzerepConstraint() {
        try {
            em.createNativeQuery(
                    "ALTER TABLE felhasznalok DROP CONSTRAINT IF EXISTS felhasznalok_szerep_check"
            ).executeUpdate();
            LOG.debug("felhasznalok_szerep_check constraint eltávolítva (ha létezett).");
        } catch (Exception ex) {
            LOG.warnf("Nem sikerült törölni a szerep CHECK constraint-et: %s", ex.getMessage());
        }
    }

    /**
     * Korábbi verziókban az üres cím ("") helyrajzi_szam-ként kerülhetett a DB-be.
     * PostgreSQL a UNIQUE constraint-nél az üres stringet értékként kezeli → duplikált sorokat blokkol.
     * Induláskor az üres stringeket NULL-ra alakítjuk.
     */
    private void fixHelyrajziSzamEmpty() {
        try {
            int updated = em.createNativeQuery(
                    "UPDATE ingatlanok SET helyrajzi_szam = NULL WHERE helyrajzi_szam = ''"
            ).executeUpdate();
            if (updated > 0) {
                LOG.infof("fixHelyrajziSzam: %d sor helyrajzi_szam mezője NULL-ra javítva.", updated);
            }
        } catch (Exception ex) {
            LOG.warnf("fixHelyrajziSzam: nem sikerült: %s", ex.getMessage());
        }
    }

    private void initIroda() {
        if (irodaRepository.count() > 0) return;

        Iroda demo = new Iroda();
        demo.nev = "Demo Iroda";
        demo.slug = "demo";
        demo.leiras = "Ez egy bemutató iroda az IngatlanCRM rendszer teszteléséhez.";
        demo.email = "demo@ingatlancrm.hu";
        demo.telefon = "+36 1 234 5678";
        demo.cim = "1051 Budapest, Vörösmarty tér 1.";
        demo.szinElsodleges = "#4F46E5";
        demo.szinMasodlagos = "#7C3AED";
        irodaRepository.persist(demo);

        LOG.info("================================================");
        LOG.info("  Demo iroda létrehozva: 'Demo Iroda' (slug: demo)");
        LOG.info("================================================");
    }

    private void initAdmin() {
        if (felhasznaloRepository.count() > 0) return;

        Iroda demo = irodaRepository.find("slug", "demo").firstResult();

        // Rendszer-szintű admin (nincs irodája)
        Felhasznalo admin = new Felhasznalo();
        admin.felhasznalonev = "admin";
        admin.jelszoHash = BCrypt.withDefaults().hashToString(12, "admin123".toCharArray());
        admin.szerep = Felhasznalo.FelhasznaloSzerep.ADMIN;
        admin.nev = "Rendszer Adminisztrátor";
        admin.irodaId = null;
        felhasznaloRepository.persist(admin);

        // Demo irodavezető
        if (demo != null) {
            Felhasznalo vezeto = new Felhasznalo();
            vezeto.felhasznalonev = "irodavezeto";
            vezeto.jelszoHash = BCrypt.withDefaults().hashToString(12, "demo1234".toCharArray());
            vezeto.szerep = Felhasznalo.FelhasznaloSzerep.IRODAVEZETO;
            vezeto.nev = "Demo Irodavezető";
            vezeto.irodaId = demo.id;
            felhasznaloRepository.persist(vezeto);

            // Demo referens
            Felhasznalo referens = new Felhasznalo();
            referens.felhasznalonev = "referens";
            referens.jelszoHash = BCrypt.withDefaults().hashToString(12, "demo1234".toCharArray());
            referens.szerep = Felhasznalo.FelhasznaloSzerep.REFERENS;
            referens.nev = "Demo Referens";
            referens.irodaId = demo.id;
            felhasznaloRepository.persist(referens);
        }

        LOG.info("=================================================");
        LOG.info("  Kezdeti felhasználók létrehozva:");
        LOG.info("  [ADMIN]       admin        / admin123");
        LOG.info("  [IRODAVEZETO] irodavezeto  / demo1234  → Demo Iroda");
        LOG.info("  [REFERENS]    referens     / demo1234  → Demo Iroda");
        LOG.info("  !! Éles üzembe helyezés előtt cseréld le! !!");
        LOG.info("=================================================");
    }
}
