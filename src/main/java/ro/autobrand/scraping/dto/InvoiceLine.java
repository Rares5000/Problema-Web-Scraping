package ro.autobrand.scraping.dto;

import java.math.BigDecimal;

public record InvoiceLine(
    String productCode,
    String productName,
    BigDecimal unitPrice,
    String currency,
    BigDecimal quantity
) {
}
