package ro.autobrand.scraping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.autobrand.scraping.domain.Product;
import ro.autobrand.scraping.dto.ExchangeRate;
import ro.autobrand.scraping.dto.ProductForm;
import ro.autobrand.scraping.dto.ScrapedProduct;
import ro.autobrand.scraping.exception.ProductNotFoundException;
import ro.autobrand.scraping.repository.ProductRepository;

import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    @Transactional
    public Product upsertByName(ScrapedProduct scraped) {
        return upsertByName(scraped, null);
    }

    @Transactional
    public Product upsertByName(ScrapedProduct scraped, ExchangeRate rate) {
        return repository.findByName(scraped.name())
            .map(existing -> updateExisting(existing, scraped, rate))
            .orElseGet(() -> createNew(scraped, rate));
    }

    private Product updateExisting(Product existing, ScrapedProduct scraped, ExchangeRate rate) {
        existing.setPrice(scraped.price());
        existing.setCurrency(scraped.currency());
        existing.setImageUrl(scraped.imageUrl());
        existing.setDescription(scraped.description());
        applyExchangeRate(existing, rate);
        log.debug("Updating product '{}'", existing.getName());
        return repository.save(existing);
    }

    private Product createNew(ScrapedProduct scraped, ExchangeRate rate) {
        Product product = Product.builder()
            .name(scraped.name())
            .price(scraped.price())
            .currency(scraped.currency())
            .imageUrl(scraped.imageUrl())
            .description(scraped.description())
            .build();
        applyExchangeRate(product, rate);
        log.debug("Creating product '{}'", product.getName());
        return repository.save(product);
    }

    private void applyExchangeRate(Product product, ExchangeRate rate) {
        if (rate == null || product.getPrice() == null) {
            return;
        }
        product.setExchangeRate(rate.rate());
        product.setExchangeRateDate(rate.date());
        product.setPriceRon(product.getPrice().multiply(rate.rate()).setScale(2, RoundingMode.HALF_UP));
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Product> search(String query, Sort sort) {
        if (query == null || query.isBlank()) {
            return repository.findAll(sort);
        }
        return repository.findByNameContainingIgnoreCase(query.trim(), sort);
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public Product update(Long id, ProductForm form) {
        Product product = repository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        product.setName(form.name());
        product.setPrice(form.price());
        product.setCurrency(form.currency());
        product.setImageUrl(form.imageUrl());
        product.setDescription(form.description());
        return repository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
