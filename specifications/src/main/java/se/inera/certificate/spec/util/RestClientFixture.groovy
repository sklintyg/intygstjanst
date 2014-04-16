package se.inera.certificate.spec.util

import groovyx.net.http.RESTClient
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory

import java.security.KeyStore

import se.inera.certificate.spec.util.ssl.TestSSLSocketFactory;

/**
 *
 * @author andreaskaltenbach
 */
class RestClientFixture {

    static String baseUrl = System.getProperty("certificate.baseUrl") + "resources/"

    /**
     * Creates a RestClient which accepts all server certificates
     * @return
     */
    static def createRestClient() {
        def restClient = new RESTClient(baseUrl)

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        SSLSocketFactory sf = new TestSSLSocketFactory(trustStore);
        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        restClient.client.connectionManager.schemeRegistry.register(new Scheme("https", sf, 443))

        restClient
    }
}
