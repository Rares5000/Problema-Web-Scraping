package ro.autobrand.scraping.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "scraper")
public record ScraperProperties(
    String baseUrl,
    String loginPath,
    String productsPath,
    String category,
    String username,
    String password,
    String currency,
    Duration timeout
) {
}
