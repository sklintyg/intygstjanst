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
package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;

/**
 * @author rogerlindsjo
 */
public interface CertificateSenderService {

    /**
     * Sends given certificate to the destined target.
     *
     * @param certificate
     *            the certificate
     * @param recipientId
     *            The identifier of the recipient.
     * @throws javax.xml.ws.WebServiceException
     *             if the web service call does not succeed
     */
    void sendCertificate(Certificate certificate, String recipientId);

    /**
     * Sends a message to a recipient that a certificate has been revoked.
     *
     * @param certificate
     *            The now revoked certificate
     * @param recipientId
     *            The id of the recipient.
     * @param revokeData
     *            Data of who requested the revoke, when etc.
     */
    void sendCertificateRevocation(Certificate certificate, String recipientId, RevokeType revokeData);
}
