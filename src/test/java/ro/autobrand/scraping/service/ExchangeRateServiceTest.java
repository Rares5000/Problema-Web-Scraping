package ro.autobrand.scraping.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.Test;
import ro.autobrand.scraping.config.BnrProperties;
import ro.autobrand.scraping.dto.ExchangeRate;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateServiceTest {

    private final ExchangeRateService service = new ExchangeRateService(
        new BnrProperties("https://www.bnr.ro/nbrfxrates.xml", Duration.ofSeconds(15)),
        HttpClient.newHttpClient()
    );

    @Test
    void parsesUsdRateFromBnrXml() throws IOException {
        Document doc = loadFixture();

        Optional<ExchangeRate> rate = service.parseRate(doc, "USD");

        assertThat(rate).isPresent();
        assertThat(rate.get().currency()).isEqualTo("USD");
        assertThat(rate.get().rate()).isEqualByComparingTo("4.4991");
        assertThat(rate.get().date()).isEqualTo(LocalDate.of(2026, 5, 19));
    }

    @Test
    void appliesMultiplierWhenPresent() throws IOException {
        Document doc = loadFixture();

        Optional<ExchangeRate> rate = service.parseRate(doc, "HUF");

        assertThat(rate).isPresent();
        assertThat(rate.get().rate()).isEqualByComparingTo("0.0125");
    }

    @Test
    void returnsEmptyForUnknownCurrency() throws IOException {
        Document doc = loadFixture();

        assertThat(service.parseRate(doc, "JPY")).isEmpty();
    }

    private Document loadFixture() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("fixtures/bnr-rates.xml")) {
            return Jsoup.parse(in, "UTF-8", "", Parser.xmlParser());
        }
    }
}
