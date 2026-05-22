package ro.autobrand.scraping.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Product not found: id=" + id);
    }
}
