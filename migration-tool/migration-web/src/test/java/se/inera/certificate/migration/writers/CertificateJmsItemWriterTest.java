package se.inera.certificate.migration.writers;

import org.apache.activemq.broker.BrokerService;

public class CertificateJmsItemWriterTest {

    BrokerService broker;

    public void setup() {
        broker = new BrokerService();

    }

}
