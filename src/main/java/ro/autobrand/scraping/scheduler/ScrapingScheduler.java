package ro.autobrand.scraping.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ro.autobrand.scraping.dto.ScrapingResult;
import ro.autobrand.scraping.service.ScrapingService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScrapingScheduler {

    private final ScrapingService scrapingService;

    @Scheduled(cron = "0 0 12-18 * * *", zone = "Europe/Bucharest")
    public void scheduledScrape() {
        log.info("Cron-triggered scraping run starting");
        try {
            ScrapingResult result = scrapingService.runScraping();
            log.info("Cron-triggered scraping run finished: {} product(s) saved ({} rows scraped)",
                result.productsSaved(), result.rowsScraped());
        } catch (Exception ex) {
            log.error("Cron-triggered scraping run failed", ex);
        }
    }
}
