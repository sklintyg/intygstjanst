/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.regex.Pattern;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitConfigurer;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configures CXF outbound HTTP conduits for mutual TLS (mTLS) against NTJP.
 * Active for all profiles except {@code dev}.
 *
 * <p>The {@link HTTPConduitConfigurer} bean is auto-discovered by CXF and invoked for every
 * outbound conduit. TLS configuration is applied only to conduits whose name matches
 * {@link #NTJP_CONDUIT_NAME_PATTERN}, equivalent to the XML
 * {@code name="\{urn:riv:(clinicalprocess:healthcond|insuranceprocess:healthreporting):.*.http-conduit"}.
 */
@Configuration
@Profile("!dev")
public class CxfTlsConfig {

    /**
     * Matches CXF conduit names for RIV-TA services routed via NTJP.
     * Corresponds to the XML conduit {@code name} selector pattern.
     */
    private static final Pattern NTJP_CONDUIT_NAME_PATTERN =
        Pattern.compile("\\{urn:riv:(clinicalprocess:healthcond|insuranceprocess:healthreporting):.*\\.http-conduit");

    @Value("${ntjp.ws.certificate.file}")
    private String certFile;

    @Value("${ntjp.ws.certificate.password}")
    private String certPassword;

    @Value("${ntjp.ws.certificate.type}")
    private String certType;

    @Value("${ntjp.ws.key.manager.password}")
    private String keyManagerPassword;

    @Value("${ntjp.ws.truststore.file}")
    private String truststoreFile;

    @Value("${ntjp.ws.truststore.password}")
    private String truststorePassword;

    @Value("${ntjp.ws.truststore.type}")
    private String truststoreType;

    /**
     * Returns a {@link HTTPConduitConfigurer} that CXF invokes for every outbound conduit.
     * Only conduits whose name matches {@link #NTJP_CONDUIT_NAME_PATTERN} receive TLS config.
     *
     * <p>Equivalent to
     * {@code <http:conduit name="\{urn:riv:(clinicalprocess:healthcond|insuranceprocess:healthreporting):.*.http-conduit">} in XML.
     */
    @Bean
    public HTTPConduitConfigurer cxfConduitConfigurer() {
        return (name, address, conduit) -> {
            if (name != null && NTJP_CONDUIT_NAME_PATTERN.matcher(name).find()) {
                configureConduit(conduit);
            }
        };
    }

    private void configureConduit(HTTPConduit conduit) {
        configureClientPolicy(conduit);
        configureTls(conduit);
    }

    private void configureClientPolicy(HTTPConduit conduit) {
        final var clientPolicy = new HTTPClientPolicy();
        clientPolicy.setAllowChunking(false);
        clientPolicy.setAutoRedirect(true);
        clientPolicy.setConnection(ConnectionType.KEEP_ALIVE);
        conduit.setClient(clientPolicy);
    }

    private void configureTls(HTTPConduit conduit) {
        final var tls = new TLSClientParameters();
        tls.setDisableCNCheck(true);

        try {
            final var keyStore = loadKeyStore(certFile, certPassword, certType);
            final var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyManagerPassword.toCharArray());
            tls.setKeyManagers(kmf.getKeyManagers());

            final var trustStore = loadKeyStore(truststoreFile, truststorePassword, truststoreType);
            final var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            tls.setTrustManagers(tmf.getTrustManagers());

        } catch (KeyStoreException | IOException | CertificateException
                 | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new IllegalStateException("Failed to configure CXF TLS conduit", e);
        }

        // <sec:cipherSuitesFilter> … </sec:cipherSuitesFilter>
        final var cipherFilter = new FiltersType();
        cipherFilter.getInclude().addAll(List.of(
            ".*_EXPORT_.*",
            ".*_EXPORT1024_.*",
            ".*_WITH_DES_.*",
            ".*_WITH_AES_.*",
            ".*_WITH_NULL_.*"
        ));
        cipherFilter.getExclude().add(".*_DH_anon_.*");
        tls.setCipherSuitesFilter(cipherFilter);

        conduit.setTlsClientParameters(tls);
    }

    private KeyStore loadKeyStore(String file, String password, String type)
        throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        final var keyStore = KeyStore.getInstance(type);
        try (final var fis = new FileInputStream(file)) {
            keyStore.load(fis, password.toCharArray());
        }
        return keyStore;
    }
}

