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
import se.inera.intyg.common.specifications.spec.util.WsClientFixture
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType


public class InvanareSkickarIntygTillFk extends WsClientFixture {

    private SendCertificateToRecipientResponderInterface responder
    private SendCertificateToRecipientResponseType response

    static String serviceUrl = System.getProperty("service.clinicalProcess.SendCertificateToRecipientUrl")

    String personnummer
    String intyg
    String mottagare

    public InvanareSkickarIntygTillFk() {
        String url = serviceUrl ? serviceUrl : baseUrl + "send-certificate-to-recipient/v1.0"
        responder = createClient(SendCertificateToRecipientResponderInterface.class, url)
    }

    def resultat

    def execute() {
        def request = new SendCertificateToRecipientType()
        request.mottagareId = mottagare
        request.personId = personnummer
        request.utlatandeId = intyg

        response = responder.sendCertificateToRecipient(logicalAddress.value, request)

        switch (response.result.resultCode) {
            case ResultCodeType.OK:
                resultat = "OK"
                break
            default:
                resultat = "[${response.result.resultCode.toString()}] - ${response.result.resultText}"
        }
    }

    public String resultat() {
        resultat
    }

}
