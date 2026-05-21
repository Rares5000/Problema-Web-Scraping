package ro.autobrand.scraping.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.autobrand.scraping.domain.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByName(String name);
}
