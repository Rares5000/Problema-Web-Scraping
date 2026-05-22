package ro.autobrand.scraping.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.autobrand.scraping.domain.Product;
import ro.autobrand.scraping.dto.ProductForm;
import ro.autobrand.scraping.service.ProductService;
import ro.autobrand.scraping.service.ScrapingService;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ScrapingService scrapingService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "products";
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
        int count = scrapingService.runScraping();
        redirect.addFlashAttribute("message",
            "Scraping completed: " + count + " product(s) processed.");
        return "redirect:/products";
    }
}
