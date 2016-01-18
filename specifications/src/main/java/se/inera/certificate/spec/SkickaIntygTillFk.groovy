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

import iso.v21090.dt.v1.II
import org.joda.time.LocalDateTime
import se.inera.intyg.common.specifications.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.VardAdresseringsType
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.rivtabp20.v1.SendMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType

/**
 *
 * @author andreaskaltenbach
 */
class SkickaIntygTillFk extends WsClientFixture {

    private SendMedicalCertificateResponderInterface sendResponder

    String personnummer
    String intyg

    SendMedicalCertificateResponseType response

    static String serviceUrl = System.getProperty("service.sendCertificateUrl")

    public SkickaIntygTillFk() {
        super()
    }

    public SkickaIntygTillFk(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String url = serviceUrl ? serviceUrl : baseUrl + "send-certificate/v1.0"
        sendResponder = createClient(SendMedicalCertificateResponderInterface.class, url)
    }

    public void execute() {
        SendMedicalCertificateRequestType sendRequestType = new SendMedicalCertificateRequestType()
        SendType sendType = new SendType()
        sendRequestType.setSend(sendType)
        sendType.vardReferensId = 1
        sendType.avsantTidpunkt = new LocalDateTime("2013-05-01T11:00:00")

        sendType.adressVard = new VardAdresseringsType()
        sendType.adressVard.hosPersonal = new HosPersonalType()
        sendType.adressVard.hosPersonal.personalId = new II()
        sendType.adressVard.hosPersonal.personalId.root = "1.2.752.129.2.1.4.1"
        sendType.adressVard.hosPersonal.personalId.extension = "personalid"
        sendType.adressVard.hosPersonal.fullstandigtNamn = "En läkare"
        sendType.adressVard.hosPersonal.enhet = new EnhetType()
        sendType.adressVard.hosPersonal.enhet.enhetsId = new II()
        sendType.adressVard.hosPersonal.enhet.enhetsId.root = "1.2.752.129.2.1.4.1"
        sendType.adressVard.hosPersonal.enhet.enhetsId.extension = "1"
        sendType.adressVard.hosPersonal.enhet.enhetsnamn = "Enhetsnamn"
        sendType.adressVard.hosPersonal.enhet.vardgivare = new VardgivareType()
        sendType.adressVard.hosPersonal.enhet.vardgivare.vardgivareId = new II()
        sendType.adressVard.hosPersonal.enhet.vardgivare.vardgivareId.root = "1.2.752.129.2.1.4.1"
        sendType.adressVard.hosPersonal.enhet.vardgivare.vardgivareId.extension = "1"
        sendType.adressVard.hosPersonal.enhet.vardgivare.vardgivarnamn = "Vårdgivarnamn"

        sendType.lakarutlatande = new LakarutlatandeEnkelType()
        sendType.lakarutlatande.lakarutlatandeId = intyg
        sendType.lakarutlatande.signeringsTidpunkt = new LocalDateTime("2013-05-01T11:00:00")
        sendType.lakarutlatande.patient = new PatientType()
        sendType.lakarutlatande.patient.personId = new II()
        sendType.lakarutlatande.patient.personId.root = "1.2.752.129.2.1.3.1"
        sendType.lakarutlatande.patient.personId.extension = personnummer
        sendType.lakarutlatande.patient.fullstandigtNamn = "Ett namn"

        response = sendResponder.sendMedicalCertificate(logicalAddress, sendRequestType)
    }

    public String resultat() {
        resultAsString(response)
    }
}
