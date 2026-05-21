package ro.autobrand.scraping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.autobrand.scraping.domain.Product;
import ro.autobrand.scraping.dto.ScrapedProduct;
import ro.autobrand.scraping.repository.ProductRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    @Transactional
    public Product upsertByName(ScrapedProduct scraped) {
        return repository.findByName(scraped.name())
            .map(existing -> updateExisting(existing, scraped))
            .orElseGet(() -> createNew(scraped));
    }

    private Product updateExisting(Product existing, ScrapedProduct scraped) {
        existing.setPrice(scraped.price());
        existing.setCurrency(scraped.currency());
        existing.setImageUrl(scraped.imageUrl());
        existing.setDescription(scraped.description());
        log.debug("Updating product '{}'", existing.getName());
        return repository.save(existing);
    }

    private Product createNew(ScrapedProduct scraped) {
        Product product = Product.builder()
            .name(scraped.name())
            .price(scraped.price())
            .currency(scraped.currency())
            .imageUrl(scraped.imageUrl())
            .description(scraped.description())
            .build();
        log.debug("Creating product '{}'", product.getName());
        return repository.save(product);
    }
}
