package ro.autobrand.scraping.service;

import org.junit.jupiter.api.Test;
import ro.autobrand.scraping.dto.InvoiceLine;
import ro.autobrand.scraping.exception.InvoiceParsingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceProcessingServiceTest {

    private static final Path SAMPLE_INVOICE = Path.of(
        "sample-data/AD AUTO TOTAL SRL_20241747776_2024_03_01.PDF"
    );

    private final InvoiceProcessingService service = new InvoiceProcessingService();

    @Test
    void parsesLineFromExtractedText() {
        String text = """
            1 172812F COMUTATOR PORNIRE FEBI 251.96 RON -1 -1 H87 19 -251.96
            Identificator vanzator articol pentru linia 1 :172812F
            """;

        List<InvoiceLine> lines = service.parseLines(text);

        assertThat(lines).hasSize(1);
        InvoiceLine line = lines.get(0);
        assertThat(line.productCode()).isEqualTo("172812F");
        assertThat(line.productName()).isEqualTo("COMUTATOR PORNIRE FEBI");
        assertThat(line.unitPrice()).isEqualByComparingTo("251.96");
        assertThat(line.currency()).isEqualTo("RON");
        assertThat(line.quantity()).isEqualByComparingTo("-1");
    }

    @Test
    void extractsLineFromAdAutoTotalInvoice() throws IOException {
        byte[] pdf = Files.readAllBytes(SAMPLE_INVOICE);

        List<InvoiceLine> lines = service.extractLines(pdf);

        assertThat(lines).hasSize(1);
        InvoiceLine line = lines.get(0);
        assertThat(line.productCode()).isEqualTo("172812F");
        assertThat(line.productName()).contains("COMUTATOR PORNIRE FEBI");
        assertThat(line.unitPrice()).isEqualByComparingTo("251.96");
        assertThat(line.currency()).isEqualTo("RON");
        assertThat(line.quantity()).isEqualByComparingTo("-1");
    }

    @Test
    void rejectsInvalidPdfBytes() {
        byte[] notPdf = "not a pdf".getBytes();

        assertThatThrownBy(() -> service.extractLines(notPdf))
            .isInstanceOf(InvoiceParsingException.class);
    }
}
