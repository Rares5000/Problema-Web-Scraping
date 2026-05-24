package ro.autobrand.scraping.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import ro.autobrand.scraping.dto.InvoiceLine;
import ro.autobrand.scraping.exception.InvoiceParsingException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class InvoiceProcessingService {

    private static final Pattern DATA_ROW = Pattern.compile(
        "(?m)^\\s*(\\d+)\\s+(\\S+)\\s+(.+?)\\s+(-?\\d+(?:[.,]\\d+)?)\\s+([A-Z]{3})\\s+(-?\\d+(?:[.,]\\d+)?)"
    );

    private static final Pattern SELLER_ITEM_ID = Pattern.compile(
        "Identificator vanzator articol pentru linia\\s+(\\d+)\\s*:\\s*(\\S+)"
    );

    public List<InvoiceLine> extractLines(byte[] pdfBytes) {
        String text = extractText(pdfBytes);
        List<InvoiceLine> lines = parseLines(text);
        log.info("Extracted {} invoice line(s)", lines.size());
        return lines;
    }

    private String extractText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (IOException ex) {
            throw new InvoiceParsingException("Failed to read PDF", ex);
        }
    }

    List<InvoiceLine> parseLines(String text) {
        Map<Integer, String> codeByLine = new HashMap<>();
        Matcher sellerIdMatcher = SELLER_ITEM_ID.matcher(text);
        while (sellerIdMatcher.find()) {
            codeByLine.put(Integer.parseInt(sellerIdMatcher.group(1)), sellerIdMatcher.group(2));
        }

        List<InvoiceLine> lines = new ArrayList<>();
        Matcher rowMatcher = DATA_ROW.matcher(text);
        while (rowMatcher.find()) {
            int lineNo = Integer.parseInt(rowMatcher.group(1));
            String tokenAfterLineNo = rowMatcher.group(2);
            String remainder = rowMatcher.group(3).trim();
            BigDecimal unitPrice = parseNumber(rowMatcher.group(4));
            String currency = rowMatcher.group(5);
            BigDecimal quantity = parseNumber(rowMatcher.group(6));

            String code = codeByLine.getOrDefault(lineNo, tokenAfterLineNo);
            String name = code.equals(tokenAfterLineNo)
                ? remainder
                : (tokenAfterLineNo + " " + remainder);

            lines.add(new InvoiceLine(code, name, unitPrice, currency, quantity));
        }
        return lines;
    }

    private static BigDecimal parseNumber(String raw) {
        return new BigDecimal(raw.replace(',', '.'));
    }
}
