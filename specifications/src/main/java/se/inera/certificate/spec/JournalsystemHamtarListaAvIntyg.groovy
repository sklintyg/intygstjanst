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
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v2.ListCertificatesForCareResponderInterface
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v2.ListCertificatesForCareResponseType
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v2.ListCertificatesForCareType
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId

/**
 *
 * @author andreaskaltenbach
 */
public class JournalsystemHamtarListaAvIntyg extends WsClientFixture {

    private ListCertificatesForCareResponderInterface responder

    static String serviceUrl = System.getProperty("service.clinicalProcess.listCertificatesUrl")

    public JournalsystemHamtarListaAvIntyg() {
        String url = serviceUrl ? serviceUrl : baseUrl + "list-certificates-for-care/v2.0"
        responder = createClient(ListCertificatesForCareResponderInterface.class, url)
    }

    String personnummer
    String enhet

    List intygMeta
    def resultat

    private ListCertificatesForCareResponseType response

    def execute() {
        def request = new ListCertificatesForCareType()
        request.personId = new PersonId()
        request.personId.root = "1.2.752.129.2.1.3.1"
        request.personId.extension = personnummer.replaceAll('-','')
        def hsaId = new HsaId()
        hsaId.root = "1.2.752.129.2.1.4.1"
        hsaId.extension = enhet
        request.enhetsId.add(hsaId)

        response = responder.listCertificatesForCare(logicalAddress.value, request)
        switch (response.result.resultCode) {
            case ResultCodeType.OK:
                intygMeta = response.intygsLista.intyg
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
        intygMeta.collect { it.intygsId.extension }
    }
}
