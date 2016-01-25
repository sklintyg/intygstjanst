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
import se.inera.intyg.common.specifications.spec.util.WsClientFixtureNyaKontraktet
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponseType
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateType
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.RecipientType
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType

public class HamtaMottagare extends WsClientFixtureNyaKontraktet {

    private GetRecipientsForCertificateResponderInterface getRecipientsForCertificateResponder

	static String serviceUrl = System.getProperty("service.getRecipientsForCertificateUrl")

    public HamtaMottagare() {
		String url = serviceUrl ? serviceUrl : baseUrl + "get-recipients-for-certificate/v1.0"
		getRecipientsForCertificateResponder = createClient(GetRecipientsForCertificateResponderInterface.class, url)
    }

    String intygsTyp
    
    private String resultat
    private List<String> mottagare
    private GetRecipientsForCertificateResponseType response

	public void reset() {
		response = null
		resultat = null
		mottagare = null
	}

    public void execute() {
        GetRecipientsForCertificateType request = new GetRecipientsForCertificateType()
        request.certificateType = intygsTyp

        response = getRecipientsForCertificateResponder.getRecipientsForCertificate(logicalAddress.value, request)
            switch (response.result.resultCode) {
                case ResultCodeType.OK:
	            List<RecipientType> recipients = response.recipient
                mottagare = recipients.collect() { recipient -> "${recipient.id}-${recipient.name}" }
				resultat = resultAsString(response)
				break
            default:
				resultat = resultAsString(response)
		}
    }

    public List<String> mottagare() {
        return mottagare
    }

    public String resultat() {
        return resultat
    }

}
