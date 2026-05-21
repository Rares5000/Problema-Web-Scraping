package ro.autobrand.scraping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.autobrand.scraping.dto.ScrapedProduct;
import ro.autobrand.scraping.scraper.WebScrapingClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapingService {

    private final WebScrapingClient webScrapingClient;
    private final ProductService productService;

    public int runScraping() {
        log.info("Starting scraping run");
        List<ScrapedProduct> scraped = webScrapingClient.scrapeConsumables();
        scraped.forEach(productService::upsertByName);
        log.info("Scraping run complete: {} product(s) processed", scraped.size());
        return scraped.size();
    }
}
