/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.service.InternalNotificationService;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientType;

import javax.jms.Queue;
import javax.jms.TextMessage;

import static java.lang.invoke.MethodHandles.lookup;

@Service
public class InternalNotificationServiceImpl implements InternalNotificationService {

    private static final String ACTION = "action";
    private static final String SENT = "sent";

    private static final String CERTIFICATE_ID = "certificate-id";
    private static final String CERTIFICATE_TYPE = "certificate-type";
    private static final String CARE_UNIT_ID = "care-unit-id";

    private static final Logger LOG = LoggerFactory.getLogger(lookup().getClass());

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier(value = "internalNotificationQueue")
    private Queue internalNotificationQueue;

    @Override
    public void notifyCareIfSentByCitizen(Certificate certificate, SendCertificateToRecipientType.SkickatAv skickatAv) {

        // Only notify internal if citizen sent
        if (skickatAv != null && skickatAv.getPersonId() != null
                && skickatAv.getHosPersonal() == null) {
            notifyCertificateSentByCitizenToRecipient(certificate.getId(),
                    certificate.getType(), certificate.getCareUnitId());
        }
    }

    private void notifyCertificateSentByCitizenToRecipient(String certificateId, String certificateType,
            String careUnitId) {
        boolean rc = sendCertificateSentByCitizienToInternalNotificationQueue(SENT, certificateId, certificateType, careUnitId);
        if (rc) {
            LOG.debug("Internal notification was sent");
        } else {
            LOG.error("An error occured sending internal notification");
        }
    }

    private boolean sendCertificateSentByCitizienToInternalNotificationQueue(
            final String actionType,
            final String certificateId,
            final String certificateType,
            final String careUnitId) {

        try {
            return send(session -> {
                TextMessage message = session.createTextMessage("");
                message.setStringProperty(ACTION, actionType);
                message.setStringProperty(CERTIFICATE_ID, certificateId);
                message.setStringProperty(CERTIFICATE_TYPE, certificateType);
                message.setStringProperty(CARE_UNIT_ID, careUnitId);
                return message;
            });
        } catch (JmsException e) {
            LOG.error("Failure sending '{}' type with certificate id '{}'to statistics", actionType, certificateId, e);
            return false;
        }
    }

    private boolean send(final MessageCreator messageCreator) {
        jmsTemplate.send(internalNotificationQueue, messageCreator);
        return true;
    }
}
