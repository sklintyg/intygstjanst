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

package se.inera.intyg.common.specifications.spec

import org.joda.time.LocalDateTime
import se.inera.intyg.common.specifications.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.certificate.v1.StatusType
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatus.rivtabp20.v1.SetCertificateStatusResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusRequestType
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusResponseType

/**
 *
 * @author andreaskaltenbach
 */
class MarkeraIntygSomHamtat extends WsClientFixture {

    private SetCertificateStatusResponderInterface setCertificateStatusResponder

    String personnr
    String intyg

    String kommentar

    static String serviceUrl = System.getProperty("service.setCertificateStatusUrl")

	public MarkeraIntygSomHamtat() {
		String url = serviceUrl ? serviceUrl : baseUrl + "set-certificate-status/v1.0"
		setCertificateStatusResponder = createClient(SetCertificateStatusResponderInterface.class, url)
    }

    public String resultat() {
        SetCertificateStatusRequestType parameters = new SetCertificateStatusRequestType()
        parameters.nationalIdentityNumber = personnr
        parameters.certificateId = intyg

        parameters.target = "FK"
        parameters.timestamp = LocalDateTime.now()
        parameters.status = StatusType.SENT

        SetCertificateStatusResponseType response = setCertificateStatusResponder.setCertificateStatus(logicalAddress, parameters)

        resultAsString(response)
    }
}
