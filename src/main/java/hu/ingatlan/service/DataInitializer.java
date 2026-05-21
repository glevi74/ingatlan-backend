package hu.ingatlan.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hu.ingatlan.domain.Felhasznalo;
import hu.ingatlan.repository.FelhasznaloRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DataInitializer {

    private static final Logger LOG = Logger.getLogger(DataInitializer.class);

    @Inject
    FelhasznaloRepository repository;

    @Transactional
    public void onStart(@Observes StartupEvent ev) {
        if (repository.count() == 0) {
            String jelszo = "admin123";
            Felhasznalo admin = new Felhasznalo();
            admin.felhasznalonev = "admin";
            admin.jelszoHash = BCrypt.withDefaults().hashToString(12, jelszo.toCharArray());
            admin.szerep = Felhasznalo.FelhasznaloSzerep.ADMIN;
            repository.persist(admin);
            LOG.info("=================================================");
            LOG.info("  Default admin létrehozva:");
            LOG.info("  Felhasználónév: admin");
            LOG.info("  Jelszó:         admin123");
            LOG.info("  !! Éles üzembe helyezés előtt cseréld le! !!");
            LOG.info("=================================================");
        }
    }
}
