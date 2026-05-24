package ro.autobrand.scraping.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AppSecurityProperties(
    String username,
    String password
) {
}
