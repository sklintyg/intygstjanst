/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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
import se.inera.intyg.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenResponseType
import se.inera.intyg.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenType
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType

public class InvanareHamtarListaAvIntyg extends WsClientFixture {

    private ListCertificatesForCitizenResponderInterface responder

    static String serviceUrl = System.getProperty("service.clinicalProcess.listCertificatesForCitizenUrl")

    public InvanareHamtarListaAvIntyg() {
        String url = serviceUrl ? serviceUrl : baseUrl + "list-certificates-for-citizen/v1.0"
        responder = createClient(ListCertificatesForCitizenResponderInterface.class, url)
    }

    String personnummer
    String typ

    List intygMeta
    def resultat

    private ListCertificatesForCitizenResponseType response

    def execute() {
        def request = new ListCertificatesForCitizenType()
        request.personId = personnummer
        request.utlatandeTyp = [typ]

        response = responder.listCertificatesForCitizen(logicalAddress.value, request)
        switch (response.result.resultCode) {
            case ResultCodeType.OK:
                intygMeta = response.meta
                resultat = "OK"
                break
            default:
                resultat = "[${response.result.resultCode.toString()}] - ${response.result.resultText}"
        }
    }

    def String resultat() {
        resultat
    }

    def intyg() {
        intygMeta.collect { it.certificateId }.sort { it }
    }
}
