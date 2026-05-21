package ro.autobrand.scraping.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import ro.autobrand.scraping.domain.Product;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    @Test
    void persistsAndFindsByName() {
        repository.saveAndFlush(sampleProduct("Box of Chocolate Candy", "24.99"));

        Optional<Product> found = repository.findByName("Box of Chocolate Candy");

        assertThat(found).isPresent();
        assertThat(found.get().getPrice()).isEqualByComparingTo("24.99");
        assertThat(found.get().getLastUpdated()).isNotNull();
    }

    @Test
    void rejectsDuplicateName() {
        repository.saveAndFlush(sampleProduct("Red Potion", "4.99"));

        assertThatThrownBy(() ->
            repository.saveAndFlush(sampleProduct("Red Potion", "5.49"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    private Product sampleProduct(String name, String price) {
        return Product.builder()
            .name(name)
            .price(new BigDecimal(price))
            .currency("USD")
            .imageUrl("https://example.com/img.webp")
            .description("sample description")
            .build();
    }
}
