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
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DataInitializer {

    private static final Logger LOG = Logger.getLogger(DataInitializer.class);

    @Inject FelhasznaloRepository felhasznaloRepository;
    @Inject IrodaRepository irodaRepository;

    @Transactional
    public void onStart(@Observes StartupEvent ev) {
        initIroda();
        initAdmin();
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
