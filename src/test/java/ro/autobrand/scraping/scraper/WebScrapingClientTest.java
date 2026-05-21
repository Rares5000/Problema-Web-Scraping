package ro.autobrand.scraping.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import ro.autobrand.scraping.config.ScraperProperties;
import ro.autobrand.scraping.dto.ScrapedProduct;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebScrapingClientTest {

    private final WebScrapingClient client = new WebScrapingClient(testProperties());

    @Test
    void parsesProductCardsFromFixture() throws IOException {
        Document doc = loadFixture();

        List<ScrapedProduct> products = client.parseProducts(doc);

        assertThat(products).hasSize(2);
        ScrapedProduct first = products.get(0);
        assertThat(first.name()).isEqualTo("Box of Chocolate Candy");
        assertThat(first.price()).isEqualByComparingTo("24.99");
        assertThat(first.currency()).isEqualTo("USD");
        assertThat(first.imageUrl())
            .isEqualTo("https://web-scraping.dev/assets/products/orange-chocolate-box-medium-1.webp");
        assertThat(first.description()).startsWith("Indulge your sweet tooth");

        ScrapedProduct second = products.get(1);
        assertThat(second.name()).isEqualTo("Red Energy Potion");
        assertThat(second.price()).isEqualByComparingTo("4.99");
    }

    @Test
    void parsesTotalPagesFromPagingMeta() throws IOException {
        Document doc = loadFixture();

        assertThat(client.parseTotalPages(doc)).isEqualTo(4);
    }

    @Test
    void fallsBackToOnePageWhenPagingMetaMissing() {
        Document doc = Jsoup.parse("<html><body><div class='row product'></div></body></html>");

        assertThat(client.parseTotalPages(doc)).isEqualTo(1);
    }

    private Document loadFixture() throws IOException {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("fixtures/consumables-page.html")) {
            return Jsoup.parse(in, "UTF-8", "https://www.web-scraping.dev/");
        }
    }

    private static ScraperProperties testProperties() {
        return new ScraperProperties(
            "https://www.web-scraping.dev",
            "/api/login",
            "/products",
            "consumables",
            "user123",
            "password",
            "USD",
            Duration.ofSeconds(15)
        );
    }
}
