package hu.ingatlan.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDate;

/**
 * Napi 7:00-kor generálja az előző napra vonatkozó ingatlanpiaci hírsummaryt.
 */
@ApplicationScoped
public class HirScheduler {

    private static final Logger LOG = Logger.getLogger(HirScheduler.class);

    @Inject
    HirService hirService;

    /**
     * Minden nap 7:00-kor fut le.
     * Cron formátum: másodperc perc óra nap hónap hét
     */
    @Scheduled(cron = "0 0 7 * * ?")
    void generateDailyNewsSummary() {
        LocalDate tegnap = LocalDate.now().minusDays(1);
        LOG.infof("Napi hírgenerálás indul – dátum: %s", tegnap);
        try {
            hirService.generateForDate(tegnap);
            LOG.infof("Napi hírgenerálás sikeresen befejezve – dátum: %s", tegnap);
        } catch (Exception e) {
            LOG.errorf(e, "Napi hírgenerálás sikertelen – dátum: %s", tegnap);
        }
    }
}
