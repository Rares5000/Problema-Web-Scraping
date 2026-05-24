package ro.autobrand.scraping.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRate(
    String currency,
    BigDecimal rate,
    LocalDate date
) {
}
