package se.inera.certificate.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.MonitoringLogService;
import se.inera.certificate.service.StatisticsService;

@Component
public class StatisticsServiceImpl implements StatisticsService {

    private static final String CREATED = "created";
    private static final String REVOKED = "revoked";
    private static final String CERTIFICATE_ID = "certificate-id";
    private static final String ACTION = "action";

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    @Autowired(required = false)
    private JmsTemplate jmsTemplate;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Value("${statistics.enabled}")
    private boolean enabled;

    @Override
    public boolean created(Certificate certificate) {
        boolean rc = true;
        if (enabled) {
            rc = doSend(CREATED, certificate);
            if (rc) {
                monitoringLogService.logStatisticsSent(certificate.getId(), certificate.getType(), certificate.getCareUnitId());
            }
        }
        return rc;
    }

    @Override
    public boolean revoked(Certificate certificate) {
        boolean rc = true;
        if (enabled) {
            rc = doSend(REVOKED, certificate);
            if (rc) {
                monitoringLogService.logStatisticsRevoked(certificate.getId(), certificate.getType(), certificate.getCareUnitId());
            }
        }
        return rc;
    }

    private boolean doSend(String type, Certificate certificate) {
        try {
            if (jmsTemplate == null) {
                LOG.error("Failure sending certificate '{}' type '{}' to statistics, no JmsTemplate configured", certificate.getId(), type);
                return false;
            }

            MessageCreator messageCreator = new MC(type, certificate);
            jmsTemplate.send(messageCreator);
            return true;
        } catch (JmsException e) {
            LOG.error("Failure sending certificate '{}' type '{}'to statistics", certificate.getId(), type, e);
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
            TextMessage message = session.createTextMessage(certificate.getDocument());
            message.setStringProperty(ACTION, type);
            message.setStringProperty(CERTIFICATE_ID, certificate.getId());
            return message;
        }
    }
}
