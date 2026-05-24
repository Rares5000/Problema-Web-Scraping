package ro.autobrand.scraping.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.autobrand.scraping.domain.Product;
import ro.autobrand.scraping.dto.ProductForm;
import ro.autobrand.scraping.dto.ScrapingResult;
import ro.autobrand.scraping.service.ProductService;
import ro.autobrand.scraping.service.ScrapingService;

import java.util.Set;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Set<String> SORTABLE = Set.of("name", "price", "priceRon", "currency", "lastUpdated");

    private final ProductService productService;
    private final ScrapingService scrapingService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "name") String sortBy,
                       @RequestParam(defaultValue = "asc") String dir,
                       Model model) {
        Sort sort = buildSort(sortBy, dir);
        model.addAttribute("products", productService.search(search, sort));
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "products";
    }

    private Sort buildSort(String sortBy, String dir) {
        String field = SORTABLE.contains(sortBy) ? sortBy : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("id", id);
        model.addAttribute("productForm", ProductForm.from(product));
        return "product-edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("productForm") ProductForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("id", id);
            return "product-edit";
        }
        productService.update(id, form);
        redirect.addFlashAttribute("message", "Product updated.");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        productService.delete(id);
        redirect.addFlashAttribute("message", "Product deleted.");
        return "redirect:/products";
    }

    @PostMapping("/scrape")
    public String scrapeNow(RedirectAttributes redirect) {
        ScrapingResult result = scrapingService.runScraping();
        String message = result.rowsScraped() == result.productsSaved()
            ? "Scraping completed: " + result.productsSaved() + " products saved."
            : "Scraping completed: " + result.productsSaved() + " products saved ("
                + result.rowsScraped() + " rows scraped, duplicate names merged).";
        redirect.addFlashAttribute("message", message);
        return "redirect:/products";
    }
}
