# Running with HTTPS
We can make Intygstjänsten run locally with HTTPS, with certs that we can make Mina Intyg trust.

1. Edit /web/build.gradle
2. Add the following to the gretty config:


    gretty {
        // OTHER STUFF
        httpsEnabled = true
        sslKeyStorePath = '~/intyg/minaintyg-konfiguration/test/certifikat/test.intygstjanster.sjunet.org.jks'
        sslKeyStorePassword = 'CHANGEME'
        sslKeyManagerPassword = 'CHANGEME'
        sslTrustStorePath = '~/intyg/minaintyg-konfiguration/test/certifikat/truststore.jks'
        sslTrustStorePassword = 'CHANGEME'

3. E.g. update passwords. In the example above, we're using the certs from minaintyg, which should make it easy to set up a http-conduit in Mina Intyg for the very same certs.
4. ./gradlew build appRun 
