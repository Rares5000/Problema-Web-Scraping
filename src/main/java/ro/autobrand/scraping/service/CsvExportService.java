package ro.autobrand.scraping.service;

import org.springframework.stereotype.Service;
import ro.autobrand.scraping.dto.InvoiceLine;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CsvExportService {

    private static final String CRLF = "\r\n";
    private static final String HEADER = "Product code,Product name,Unit price,Currency,Quantity";

    public byte[] toCsv(List<InvoiceLine> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER).append(CRLF);
        for (InvoiceLine line : lines) {
            sb.append(escape(line.productCode())).append(',')
                .append(escape(line.productName())).append(',')
                .append(line.unitPrice()).append(',')
                .append(escape(line.currency())).append(',')
                .append(line.quantity()).append(CRLF);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(",") || value.contains("\"")
            || value.contains("\n") || value.contains("\r");
        if (!needsQuoting) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
