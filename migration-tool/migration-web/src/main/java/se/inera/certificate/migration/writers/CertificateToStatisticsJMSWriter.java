package se.inera.certificate.migration.writers;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import se.inera.certificate.migration.model.Certificate;

public class CertificateToStatisticsJMSWriter implements ItemWriter<Certificate> {

    private static final String CREATED = "created";
    private static final String REVOKED = "revoked";
    private static final String CERTIFICATE_ID = "certificate-id";
    private static final String ACTION = "action";

    private static Logger log = LoggerFactory.getLogger(CertificateToStatisticsJMSWriter.class);

    @Autowired(required = false)
    private JmsTemplate jmsTemplate;

    public CertificateToStatisticsJMSWriter() {

    }

    @Override
    public void write(List<? extends Certificate> items) throws Exception {
        
        log.debug("Received {} Certificates", items.size());
        
        for (Certificate certificate : items) {
            sendCertToStatistics(certificate);
        }

    }

    private void sendCertToStatistics(Certificate certificate) {
        
        boolean isCreated = doSend(CREATED, certificate);
        
        if (isCreated && certificate.isRevoked()) {
            log.info("Certificate '{}' is revoked", certificate.getCertificateId());
            doSend(REVOKED, certificate);
        }
    }

    private boolean doSend(String type, Certificate certificate) {
        try {

            if (jmsTemplate == null) {
                log.error("Failure sending certificate '{}' type '{}' to statistics, no JmsTemplate configured",
                        certificate.getCertificateId(), type);
                return false;
            }

            MessageCreator messageCreator = new MC(type, certificate);
            jmsTemplate.send(messageCreator);

            log.info("Certificate '{}' sent to statistics with action {}", certificate.getCertificateId(), type);

            return true;
        } catch (JmsException jmsex) {
            log.error("Failure sending certificate '{}' type '{}'to statistics", certificate.getCertificateId(), type,
                    jmsex);
            return false;
        }
    }

    private static final class MC implements MessageCreator {
        
        private final Certificate certificate;
        
        private final String type;

        public MC(String type, Certificate certificate) {
            this.type = type;
            this.certificate = certificate;
        }

        public Message createMessage(Session session) throws JMSException {
            TextMessage message = session.createTextMessage(certificate.getCertificateJson());
            message.setStringProperty(ACTION, type);
            message.setStringProperty(CERTIFICATE_ID, certificate.getCertificateId());
            return message;
        }
    }
}
