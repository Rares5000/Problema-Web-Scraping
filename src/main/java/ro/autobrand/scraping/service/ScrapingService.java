package ro.autobrand.scraping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.autobrand.scraping.config.ScraperProperties;
import ro.autobrand.scraping.dto.ExchangeRate;
import ro.autobrand.scraping.dto.ScrapedProduct;
import ro.autobrand.scraping.dto.ScrapingResult;
import ro.autobrand.scraping.scraper.WebScrapingClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapingService {

    private final WebScrapingClient webScrapingClient;
    private final ProductService productService;
    private final ExchangeRateService exchangeRateService;
    private final ScraperProperties scraperProperties;

    public ScrapingResult runScraping() {
        log.info("Starting scraping run");
        List<ScrapedProduct> scraped = webScrapingClient.scrapeConsumables();
        ExchangeRate rate = exchangeRateService.fetchRate(scraperProperties.currency()).orElse(null);
        if (rate != null) {
            log.info("Applying {}->RON rate {} ({})", rate.currency(), rate.rate(), rate.date());
        } else {
            log.warn("No exchange rate available; RON prices will not be set this run");
        }
        scraped.forEach(product -> productService.upsertByName(product, rate));

        int rowsScraped = scraped.size();
        int productsSaved = (int) scraped.stream().map(ScrapedProduct::name).distinct().count();
        log.info("Scraping run complete: {} rows scraped, {} unique product(s) saved", rowsScraped, productsSaved);
        return new ScrapingResult(rowsScraped, productsSaved);
    }
}
