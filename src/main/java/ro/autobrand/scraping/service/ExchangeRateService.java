package ro.autobrand.scraping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Service;
import ro.autobrand.scraping.config.BnrProperties;
import ro.autobrand.scraping.dto.ExchangeRate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final BnrProperties properties;
    private final HttpClient bnrHttpClient;

    public Optional<ExchangeRate> fetchRate(String currency) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.feedUrl()))
                .timeout(properties.timeout())
                .GET()
                .build();
            HttpResponse<String> response = bnrHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("BNR feed returned HTTP {}", response.statusCode());
                return Optional.empty();
            }
            Document doc = Jsoup.parse(response.body(), "", Parser.xmlParser());
            return parseRate(doc, currency);
        } catch (IOException ex) {
            log.error("Failed to fetch BNR exchange rate from {}", properties.feedUrl(), ex);
            return Optional.empty();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while fetching BNR exchange rate", ex);
            return Optional.empty();
        }
    }

    Optional<ExchangeRate> parseRate(Document doc, String currency) {
        Element cube = doc.getElementsByTag("Cube").first();
        if (cube == null) {
            log.warn("BNR feed has no <Cube> element");
            return Optional.empty();
        }
        LocalDate date = LocalDate.parse(cube.attr("date"));
        for (Element rate : cube.getElementsByTag("Rate")) {
            if (currency.equalsIgnoreCase(rate.attr("currency"))) {
                BigDecimal value = new BigDecimal(rate.text().trim());
                String multiplier = rate.attr("multiplier");
                if (!multiplier.isBlank()) {
                    value = value.divide(new BigDecimal(multiplier), 6, RoundingMode.HALF_UP);
                }
                return Optional.of(new ExchangeRate(currency.toUpperCase(), value, date));
            }
        }
        log.warn("BNR feed has no rate for currency {}", currency);
        return Optional.empty();
    }
}
