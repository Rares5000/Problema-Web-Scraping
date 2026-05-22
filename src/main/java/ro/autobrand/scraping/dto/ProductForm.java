package ro.autobrand.scraping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ro.autobrand.scraping.domain.Product;

import java.math.BigDecimal;

public record ProductForm(
    @NotBlank @Size(max = 255) String name,
    @NotNull @PositiveOrZero BigDecimal price,
    @Size(max = 3) String currency,
    @Size(max = 1000) String imageUrl,
    @Size(max = 2000) String description
) {

    public static ProductForm from(Product product) {
        return new ProductForm(
            product.getName(),
            product.getPrice(),
            product.getCurrency(),
            product.getImageUrl(),
            product.getDescription()
        );
    }
}
