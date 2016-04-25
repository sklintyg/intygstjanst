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
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v1.SendCertificateToRecipientResponderInterface
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v1.SendCertificateToRecipientResponseType
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v1.SendCertificateToRecipientType
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v1.SendCertificateToRecipientType.SkickatAv
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Part
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId
import org.joda.time.LocalDateTime


public class SkickaIntygTillMottagare extends WsClientFixture {

    private SendCertificateToRecipientResponderInterface responder
    private SendCertificateToRecipientResponseType response

    static String serviceUrl = System.getProperty("service.clinicalProcess.SendCertificateToRecipientUrl")

    String personnummer
    String intyg
    String mottagare

    public SkickaIntygTillMottagare() {
        String url = serviceUrl ? serviceUrl : baseUrl + "send-certificate-to-recipient/v1.0"
        responder = createClient(SendCertificateToRecipientResponderInterface.class, url)
    }

    def resultat

    def execute() {
        def request = new SendCertificateToRecipientType()
        request.skickatTidpunkt = LocalDateTime.now()
        request.mottagare = new Part()
        request.mottagare.code = mottagare
        request.mottagare.codeSystem = "769bb12b-bd9f-4203-a5cd-fd14f2eb3b80"
        request.patientPersonId = new PersonId()
        request.patientPersonId.root = "1.2.752.129.2.1.3.1"
        request.patientPersonId.extension = personnummer
        request.intygsId = new IntygId()
        request.intygsId.root = "enhet-hsaid"
        request.intygsId.extension = intyg
        request.skickatAv = new SkickatAv()
        request.skickatAv.personId = new PersonId()
        request.skickatAv.personId.root = "1.2.752.129.2.1.3.1"
        request.skickatAv.personId.extension = 191212121212

        response = responder.sendCertificateToRecipient(logicalAddress.value, request)

        resultat = response.result.resultCode.toString();
    }

    public String resultat() {
        resultat
    }

}
