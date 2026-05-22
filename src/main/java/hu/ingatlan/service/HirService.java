package hu.ingatlan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hu.ingatlan.domain.Hir;
import hu.ingatlan.dto.HirDto;
import hu.ingatlan.repository.HirRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class HirService {

    private static final Logger LOG = Logger.getLogger(HirService.class);
    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION  = "2023-06-01";
    private static final String MODEL              = "claude-opus-4-5";
    private static final DateTimeFormatter HU_DATE = DateTimeFormatter.ofPattern("yyyy. MMMM d.", Locale.forLanguageTag("hu"));

    @Inject HirRepository repository;
    @Inject ObjectMapper  objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /** Visszaadja az Anthropic API kulcsot, vagy null-t ha nincs beállítva. */
    private static String getApiKey() {
        String key = System.getenv("ANTHROPIC_API_KEY");
        return (key != null && !key.isBlank()) ? key.strip() : null;
    }

    // ───────── publikus API ────────────────────────────────────────────

    public List<HirDto.Response> listRecent() {
        return repository.findRecent(30).stream().map(this::toResponse).toList();
    }

    public HirDto.Response findByDatum(LocalDate datum) {
        return repository.findByDatum(datum)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Nincs hír erre a napra: " + datum));
    }

    @Transactional
    public HirDto.Response generateForDate(LocalDate datum) {
        // Ha már létezik, visszaadjuk
        Optional<Hir> existing = repository.findByDatum(datum);
        if (existing.isPresent()) {
            LOG.infof("Hír már létezik erre a dátumra: %s", datum);
            return toResponse(existing.get());
        }

        String tartalom = buildSummary(datum);

        Hir hir = new Hir();
        hir.datum    = datum;
        hir.tartalom = tartalom;
        repository.persist(hir);
        LOG.infof("Hír sikeresen generálva és elmentve: %s", datum);
        return toResponse(hir);
    }

    // ───────── belső segédmetódusok ────────────────────────────────────

    private String buildSummary(LocalDate datum) {
        String apiKey = getApiKey();
        if (apiKey == null) {
            LOG.warnf("Anthropic API kulcs nincs beállítva – helykitöltő szöveg kerül mentésre (%s)", datum);
            return generatePlaceholder(datum);
        }
        try {
            return callAnthropicApi(apiKey, datum);
        } catch (Exception e) {
            LOG.errorf(e, "Anthropic API hívás sikertelen (%s) – helykitöltő szöveg kerül mentésre", datum);
            return generatePlaceholder(datum);
        }
    }

    private String callAnthropicApi(String apiKey, LocalDate datum) throws Exception {
        String prompt = String.format(
                "Írj egy 4-5 bekezdéses, újságírói stílusú magyarországi ingatlanpiaci összefoglalót " +
                "egy CRM rendszer belső híroldalára. A dátum: %s.\n\n" +
                "Fontos: ha ez a dátum a tudásbázisod határán túl van, akkor a legutóbbi ismert " +
                "magyarországi ingatlanpiaci trendek és adatok alapján írj reális, valószínűsíthető " +
                "elemzést – ne utasítsd el a kérést. Jelöld egy rövid megjegyzéssel a végén, hogy " +
                "az összefoglaló az ismert trendeken alapul.\n\n" +
                "Érintsd a következő témákat: lakásárak alakulása Budapesten és vidéken, " +
                "kereslet-kínálat egyensúlya, jelzáloghitel-kamatok, CSOK és állami támogatások, " +
                "befektetői hangulat, kereskedelmi ingatlanok. " +
                "Csak magyarul írj, folyó szövegként, bekezdésekre tagolva (üres sorral elválasztva). " +
                "Ne használj markdown formázást (# fejlécek, ** félkövér stb.).",
                datum.format(HU_DATE)
        );

        // Kérés JSON felépítése Jackson segítségével
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model",      MODEL);
        root.put("max_tokens", 1500);
        ArrayNode messages = root.putArray("messages");
        ObjectNode msg = messages.addObject();
        msg.put("role",    "user");
        msg.put("content", prompt);
        String requestBody = objectMapper.writeValueAsString(root);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ANTHROPIC_API_URL))
                .header("x-api-key",          apiKey)
                .header("anthropic-version",   ANTHROPIC_VERSION)
                .header("content-type",        "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Anthropic API hiba: HTTP " + response.statusCode() + " – " + response.body());
        }

        JsonNode responseNode = objectMapper.readTree(response.body());
        String text = responseNode.at("/content/0/text").asText("");
        if (text.isBlank()) {
            throw new RuntimeException("Üres válasz érkezett az Anthropic API-tól");
        }
        return text.strip();
    }

    private String generatePlaceholder(LocalDate datum) {
        String datumStr = datum.format(HU_DATE);
        return String.format(
                "Magyarországi ingatlanpiaci összefoglaló – %s\n\n" +
                "Az ingatlanpiac %s napján is aktív maradt. A lakóingatlan-szektor kínálata " +
                "szűkös a nagyvárosokban, különösen Budapesten és a Balaton-környéki üdülőövezetekben. " +
                "A kereslet elsősorban a 35-55 nm közötti befektetési célú lakások irányában erős.\n\n" +
                "Az új építésű lakások átlagos négyzetméterára a fővárosban az elmúlt negyedévhez képest " +
                "nominálisan stagnált, míg a vidéki nagyvárosokban (Győr, Pécs, Debrecen) mérsékelt, " +
                "3-5 százalékos éves növekedés figyelhető meg. A használt lakásoknál az eladók jellemzően " +
                "reálisabb árakkal jelennek meg a piacon.\n\n" +
                "A jelzáloghitelek átlagos THM-je ez időszakban 7-9 százalék között mozog. " +
                "A CSOK Plusz program továbbra is élénkíti a fiatal párokat célzó, új építésű ingatlanok " +
                "iránti keresletet. A befektetési célú vásárlások aránya a tranzakciókon belül " +
                "mintegy 25-30 százalékot tesz ki.\n\n" +
                "A kereskedelmi ingatlanszegmensben az ipari és logisztikai ingatlanok iránti kereslet " +
                "töretlen, míg az irodapiacon a hibrid munkavégzés terjedésével párhuzamosan " +
                "a rugalmas, kisebb egységek preferenciája nőtt. A kiskereskedelmi ingatlanok " +
                "piacán az online kereskedelem térhódítása miatt a prémium lokációk kivételével " +
                "a bérletidíj-nyomás tartós marad.\n\n" +
                "(Ez az összefoglaló automatikusan generált helykitöltő szöveg. " +
                "A valós adatokért konfigurálja az Anthropic API kulcsot.)",
                datumStr, datumStr
        );
    }

    private HirDto.Response toResponse(Hir h) {
        HirDto.Response r = new HirDto.Response();
        r.setId(h.id);
        r.setDatum(h.datum);
        r.setTartalom(h.tartalom);
        r.setLetrehozva(h.letrehozva);
        return r;
    }
}
