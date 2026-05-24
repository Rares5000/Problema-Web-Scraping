package ro.autobrand.scraping.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.autobrand.scraping.exception.InvoiceParsingException;
import ro.autobrand.scraping.exception.ProductNotFoundException;
import ro.autobrand.scraping.exception.ScrapingException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public String handleProductNotFound(ProductNotFoundException ex, RedirectAttributes redirect) {
        log.warn("{}", ex.getMessage());
        redirect.addFlashAttribute("error", "Product not found.");
        return "redirect:/products";
    }

    @ExceptionHandler(ScrapingException.class)
    public String handleScrapingFailure(ScrapingException ex, RedirectAttributes redirect) {
        log.error("Scraping failed", ex);
        redirect.addFlashAttribute("error", "Scraping failed: " + ex.getMessage());
        return "redirect:/products";
    }

    @ExceptionHandler(InvoiceParsingException.class)
    public String handleInvoiceFailure(InvoiceParsingException ex, RedirectAttributes redirect) {
        log.error("Invoice parsing failed", ex);
        redirect.addFlashAttribute("error", "Invoice parsing failed: " + ex.getMessage());
        return "redirect:/invoice";
    }
}
