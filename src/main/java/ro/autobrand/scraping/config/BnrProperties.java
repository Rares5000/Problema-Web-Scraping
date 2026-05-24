package ro.autobrand.scraping.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "bnr")
public record BnrProperties(
    String feedUrl,
    Duration timeout
) {
}
