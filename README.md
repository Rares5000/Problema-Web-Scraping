# AutoBrand — Web Scraping & PDF Invoice Processing

Solution for the AutoBrand practical test. A Spring Boot application that:

1. Logs in to [web-scraping.dev](https://web-scraping.dev), scrapes the **consumables**
   products, stores them in a database and exposes a web UI to manage them.
2. Extracts line items from a PDF invoice and returns them as a **CSV** file.
3. (Bonus) Enriches products with the **BNR** exchange rate and a RON price,
   adds **filtering/sorting**, and protects the UI with **authentication**.

## Features

| Requirement | Where |
|---|---|
| Login + scrape `consumables` (image, name, price, description) with one library (Jsoup) | `scraper/WebScrapingClient` |
| Hourly cron between 12:00–18:00 | `scheduler/ScrapingScheduler` |
| Database with a unique product name | `domain/Product` (unique `name`) + `service/ProductService` (upsert) |
| Web UI to list / edit / delete | `web/ProductController` + Thymeleaf templates |
| Upload a PDF invoice → extract code, name, unit price, currency, quantity → CSV | `web/InvoiceController`, `service/InvoiceProcessingService`, `service/CsvExportService` |
| **Bonus** — BNR exchange rate + RON price | `service/ExchangeRateService` |
| **Bonus** — filtering & sorting | `web/ProductController` + `products.html` |
| **Bonus** — authentication | `config/SecurityConfig` |

## Tech stack

- **Java 17**, **Spring Boot 4**, Maven (with the `mvnw` wrapper)
- **Spring Data JPA** + **H2** (embedded, file-based in `./data/`)
- **Jsoup** — login + HTML scraping (and parsing the BNR XML feed)
- **Apache PDFBox** — PDF text extraction
- **Thymeleaf** — server-side rendered UI
- **Spring Security** — form-based authentication
- **JUnit 5 + Mockito + AssertJ** — tests

## Architecture

Layered: `web → service → repository → domain`.

```
ro.autobrand.scraping
├── config/      typed @ConfigurationProperties, Spring Security, scheduling, BNR HTTP client
├── domain/      Product (JPA entity, single source of truth for the schema)
├── repository/  ProductRepository (Spring Data)
├── scraper/     WebScrapingClient (Jsoup login + paginated scrape)
├── service/     ScrapingService (orchestrator), ProductService, ExchangeRateService,
│                InvoiceProcessingService, CsvExportService
├── scheduler/   ScrapingScheduler (@Scheduled)
├── web/         controllers + GlobalExceptionHandler (@ControllerAdvice)
├── dto/         immutable records (ScrapedProduct, InvoiceLine, ExchangeRate, ProductForm)
└── exception/   domain exceptions
```

Key decisions: the JPA entity is never bound to forms (a validated `ProductForm` record is used
instead); configuration is externalized and typed; both the cron and the manual trigger reuse the
single `ScrapingService.runScraping()` entry point.

## Prerequisites

- JDK 17 or newer (no local Maven needed — the project ships the Maven Wrapper).

## Run

```bash
./mvnw spring-boot:run
```

Open <http://localhost:8080> and sign in:

| Username | Password |
|----------|----------|
| `admin`  | `admin`  |

Then click **Scrape now** to populate the products (the cron only runs between 12:00–18:00).

## Using the app

- **Products** (`/products`) — list, **edit**, **delete**, search by name, sort by
  Name / Price / Price (RON). **Scrape now** triggers a scrape on demand.
- **Invoice** (`/invoice`) — upload a PDF invoice; the app extracts the line items and
  downloads a CSV (`Product code, Product name, Unit price, Currency, Quantity`).
  A sample invoice is provided in [`sample-data/`](sample-data/).
- **Sign out** — top-right of the header.

## Scheduled scraping (cron)

`ScrapingScheduler` runs every hour on the hour between 12:00 and 18:00 (Europe/Bucharest):

```
@Scheduled(cron = "0 0 12-18 * * *", zone = "Europe/Bucharest")
```

Equivalent OS crontab line (if you prefer an external scheduler hitting an endpoint):

```
0 12-18 * * *   # minute 0, hours 12..18
```

## Bonus — BNR exchange rate

On every scrape the app fetches the daily reference rates from
`https://www.bnr.ro/nbrfxrates.xml`, reads the USD rate and stores, per product, the rate,
its date and the price converted to RON.

> **Note on TLS:** `bnr.ro` is signed by the Romanian *certSIGN* CA, which is not bundled in the
> JDK default trust store. A dedicated HTTP client (`config/BnrHttpClientConfig`) trusts the
> bundled certSIGN CA certificate (`src/main/resources/certs/certsign-web-ca.pem`) — TLS
> verification stays enabled; only this one feed uses the extra trust anchor.

## Configuration

All settings live in `src/main/resources/application.yml`:

```yaml
scraper:        # web-scraping.dev base URL, credentials, category, timeout
bnr:            # BNR feed URL + timeout
app.auth:       # web app username / password (admin / admin)
spring.datasource / spring.jpa   # embedded H2
```

## Database

Embedded H2, persisted to `./data/autobrand.mv.db` (git-ignored). The schema is generated from the
JPA entity (`ddl-auto=update`). H2 console: <http://localhost:8080/h2-console>
(JDBC URL `jdbc:h2:file:./data/autobrand`, user `sa`, empty password).

## Tests

```bash
./mvnw verify
```

Focused tests cover the riskiest logic: the PDF invoice parser (against the real sample invoice),
the scraper HTML parsing (HTML fixture), the BNR XML parsing, the upsert-by-name rule, and the
unique-name database constraint.

## Assumptions

- web-scraping.dev prices have no currency symbol; they are treated as **USD** (configurable via
  `scraper.currency`), then converted to RON using the BNR USD rate.
- The invoice parser targets the Romanian *RO eFactura* layout (as in the provided sample) and
  supports negative quantities (storno) and `.`/`,` decimal separators.
