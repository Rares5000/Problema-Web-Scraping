package ro.autobrand.scraping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Configuration
public class BnrHttpClientConfig {

    // bnr.ro is signed by the Romanian certSIGN CA, which is not bundled in the JDK
    // default trust store. This dedicated client connects only to bnr.ro, so it trusts
    // the bundled certSIGN CA explicitly instead of disabling TLS verification.
    @Bean
    public HttpClient bnrHttpClient(BnrProperties properties) {
        return HttpClient.newBuilder()
            .connectTimeout(properties.timeout())
            .sslContext(buildSslContext())
            .build();
    }

    private SSLContext buildSslContext() {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate caCertificate;
            try (InputStream in = new ClassPathResource("certs/certsign-web-ca.pem").getInputStream()) {
                caCertificate = (X509Certificate) factory.generateCertificate(in);
            }

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry("certsign-web-ca", caCertificate);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (Exception ex) {
            throw new IllegalStateException("Could not build SSL context for the BNR feed", ex);
        }
    }
}
