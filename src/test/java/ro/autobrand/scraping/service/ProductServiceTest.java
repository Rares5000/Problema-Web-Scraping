package ro.autobrand.scraping.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.autobrand.scraping.domain.Product;
import ro.autobrand.scraping.dto.ScrapedProduct;
import ro.autobrand.scraping.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Test
    void createsNewProductWhenNameNotFound() {
        ScrapedProduct scraped = new ScrapedProduct(
            "Box of Chocolate Candy", new BigDecimal("24.99"), "USD",
            "https://example.com/box.webp", "Sweet treats."
        );
        when(repository.findByName("Box of Chocolate Candy")).thenReturn(Optional.empty());
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = service.upsertByName(scraped);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(repository).save(captor.capture());
        Product saved = captor.getValue();
        assertThat(saved.getId()).isNull();
        assertThat(saved.getName()).isEqualTo("Box of Chocolate Candy");
        assertThat(saved.getPrice()).isEqualByComparingTo("24.99");
        assertThat(saved.getCurrency()).isEqualTo("USD");
        assertThat(result).isSameAs(saved);
    }

    @Test
    void updatesExistingProductInsteadOfCreatingDuplicate() {
        Product existing = Product.builder()
            .id(42L)
            .name("Red Energy Potion")
            .price(new BigDecimal("4.99"))
            .currency("USD")
            .imageUrl("https://example.com/old.webp")
            .description("old description")
            .build();
        ScrapedProduct scraped = new ScrapedProduct(
            "Red Energy Potion", new BigDecimal("5.49"), "USD",
            "https://example.com/new.webp", "fresh description"
        );
        when(repository.findByName("Red Energy Potion")).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = service.upsertByName(scraped);

        verify(repository).save(existing);
        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getPrice()).isEqualByComparingTo("5.49");
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/new.webp");
        assertThat(result.getDescription()).isEqualTo("fresh description");
    }
}
