/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

public interface MonitoringLogService {

    void logCertificateRegistered(String certificateId, String certificateType, String careUnit);
    void logCertificateSent(String certificateId, String certificateType, String careUnit, String recipient);
    void logCertificateSentAndNotifiedByWiretapping(String certificateId, String certificateType, String careUnit, String recipient);
    void logCertificateRevoked(String certificateId, String certificateType, String careUnit);
    void logCertificateRevokeSent(String certificateId, String certificateType, String careUnit, String recipientId);
    void logCertificateListedByCitizen(Personnummer citizenId);
    void logCertificateListedByCare(Personnummer citizenId);
    void logCertificateStatusChanged(String certificateId, String status);
    void logConsentGiven(Personnummer citizenId);
    void logConsentRevoked(Personnummer citizenId);
    void logStatisticsSent(String certificateId, String certificateType, String careUnit);
    void logStatisticsRevoked(String certificateId, String certificateType, String careUnit);
    void logSendMessageToCareReceived(String messageId, String careUnit);
    void logSendMessageToRecipient(String messageId, String recipient);
}
