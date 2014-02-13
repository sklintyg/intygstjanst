package se.inera.certificate.migration.writers;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import se.inera.certificate.migration.model.Certificate;
import se.inera.certificate.migration.processors.AbstractCertificateProcessingException;
import se.inera.certificate.migration.processors.FatalCertificateProcessingException;

public class CertificateToStatisticsJMSWriter implements ItemWriter<Certificate>, ChunkListener {

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

    private void sendCertToStatistics(Certificate certificate) throws AbstractCertificateProcessingException {

        boolean isCreated = doSend(CREATED, certificate);

        if (isCreated && certificate.isRevoked()) {
            log.info("Certificate '{}' is revoked, adding revoke message", certificate.getCertificateId());
            doSend(REVOKED, certificate);
        }
    }

    private boolean doSend(String actionType, Certificate certificate) throws AbstractCertificateProcessingException {
        try {

            if (jmsTemplate == null) {
                log.error("Failure sending certificate '{}' type '{}' to statistics, no JmsTemplate configured",
                        certificate.getCertificateId(), actionType);
                throw new FatalCertificateProcessingException("No JmsTemplate configured!");
            }

            MessageCreator messageCreator = new MC(actionType, certificate);
            jmsTemplate.send(messageCreator);

            log.info("Certificate '{}' sent to statistics with action {}", certificate.getCertificateId(), actionType);

            return true;
        } catch (JmsException jmsex) {
            log.error("Failure sending certificate '{}' type '{}' to statistics", certificate.getCertificateId(),
                    actionType, jmsex);
            throw new FatalCertificateProcessingException("JMS failure occured when sending certificate", jmsex);
        }
    }

    private static final class MC implements MessageCreator {

        private final Certificate certificate;

        private final String actionType;

        public MC(String actionType, Certificate certificate) {
            this.actionType = actionType;
            this.certificate = certificate;
        }

        public Message createMessage(Session session) throws JMSException {
            TextMessage message = session.createTextMessage(certificate.getCertificateJson());
            message.setStringProperty(ACTION, actionType);
            message.setStringProperty(CERTIFICATE_ID, certificate.getCertificateId());
            return message;
        }
    }

    @Override
    public void beforeChunk() {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterChunk() {
        // TODO Auto-generated method stub

    }
}
