package ro.autobrand.scraping.scraper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import ro.autobrand.scraping.config.ScraperProperties;
import ro.autobrand.scraping.dto.ScrapedProduct;
import ro.autobrand.scraping.exception.ScrapingException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebScrapingClient {

    private static final Pattern TOTAL_PAGES = Pattern.compile("in\\s+(\\d+)\\s+pages?");

    private final ScraperProperties properties;

    public List<ScrapedProduct> scrapeConsumables() {
        try {
            Map<String, String> cookies = login();
            log.info("Logged in to {} as {}", properties.baseUrl(), properties.username());

            Document firstPage = fetchPage(1, cookies);
            int totalPages = parseTotalPages(firstPage);

            List<ScrapedProduct> all = new ArrayList<>(parseProducts(firstPage));
            for (int page = 2; page <= totalPages; page++) {
                all.addAll(parseProducts(fetchPage(page, cookies)));
            }
            log.info("Scraped {} products across {} page(s)", all.size(), totalPages);
            return all;
        } catch (IOException ex) {
            throw new ScrapingException("Failed to scrape consumables", ex);
        }
    }

    private Map<String, String> login() throws IOException {
        Connection.Response response = Jsoup.connect(properties.baseUrl() + properties.loginPath())
            .timeout(timeoutMillis())
            .ignoreContentType(true)
            .ignoreHttpErrors(true)
            .followRedirects(true)
            .method(Connection.Method.POST)
            .data("username", properties.username())
            .data("password", properties.password())
            .execute();
        if (response.statusCode() >= 400) {
            throw new ScrapingException("Login failed with HTTP " + response.statusCode());
        }
        return new HashMap<>(response.cookies());
    }

    private Document fetchPage(int page, Map<String, String> cookies) throws IOException {
        String url = properties.baseUrl() + properties.productsPath()
            + "?category=" + properties.category() + "&page=" + page;
        IOException lastError = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return Jsoup.connect(url)
                    .timeout(timeoutMillis())
                    .cookies(cookies)
                    .get();
            } catch (IOException ex) {
                lastError = ex;
                log.warn("Fetch attempt {} for {} failed: {}", attempt, url, ex.getMessage());
            }
        }
        throw lastError;
    }

    List<ScrapedProduct> parseProducts(Document doc) {
        List<ScrapedProduct> products = new ArrayList<>();
        for (Element card : doc.select("div.row.product")) {
            String name = card.selectFirst("h3.mb-0 a").text();
            String imageUrl = card.selectFirst("img").attr("src");
            String description = card.selectFirst("div.short-description").text();
            String priceText = card.selectFirst("div.price").text().replaceAll("[^0-9.]", "");
            BigDecimal price = new BigDecimal(priceText);
            products.add(new ScrapedProduct(name, price, properties.currency(), imageUrl, description));
        }
        return products;
    }

    int parseTotalPages(Document doc) {
        Element pagingMeta = doc.selectFirst("div.paging-meta");
        if (pagingMeta == null) {
            return 1;
        }
        Matcher matcher = TOTAL_PAGES.matcher(pagingMeta.text());
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 1;
    }

    private int timeoutMillis() {
        return (int) properties.timeout().toMillis();
    }
}
