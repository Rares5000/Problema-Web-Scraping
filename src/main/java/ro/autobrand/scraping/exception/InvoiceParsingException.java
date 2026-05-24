package ro.autobrand.scraping.exception;

public class InvoiceParsingException extends RuntimeException {

    public InvoiceParsingException(String message) {
        super(message);
    }

    public InvoiceParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
