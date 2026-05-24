package ro.autobrand.scraping.dto;

public record ScrapingResult(
    int rowsScraped,
    int productsSaved
) {
}
