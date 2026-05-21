package ro.autobrand.scraping.dto;

import java.math.BigDecimal;

public record ScrapedProduct(
    String name,
    BigDecimal price,
    String currency,
    String imageUrl,
    String description
) {
}
