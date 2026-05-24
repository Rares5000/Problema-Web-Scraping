package ro.autobrand.scraping.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ro.autobrand.scraping.dto.InvoiceLine;
import ro.autobrand.scraping.exception.InvoiceParsingException;
import ro.autobrand.scraping.service.CsvExportService;
import ro.autobrand.scraping.service.InvoiceProcessingService;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceProcessingService invoiceProcessingService;
    private final CsvExportService csvExportService;

    @GetMapping
    public String form() {
        return "invoice";
    }

    @PostMapping("/upload")
    public ResponseEntity<byte[]> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvoiceParsingException("No file uploaded");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new InvoiceParsingException("Could not read uploaded file", ex);
        }
        List<InvoiceLine> lines = invoiceProcessingService.extractLines(bytes);
        if (lines.isEmpty()) {
            throw new InvoiceParsingException("No invoice lines found in the PDF");
        }
        byte[] csv = csvExportService.toCsv(lines);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice.csv\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csv);
    }
}
