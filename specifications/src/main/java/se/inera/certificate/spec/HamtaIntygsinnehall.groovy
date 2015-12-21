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

import org.skyscreamer.jsonassert.JSONAssert
import se.inera.intyg.common.specifications.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum
import se.inera.intyg.insuranceprocess.healthreporting.getcertificatecontent.rivtabp20.v1.GetCertificateContentResponderInterface
import se.inera.intyg.insuranceprocess.healthreporting.getcertificatecontentresponder.v1.GetCertificateContentRequestType
import se.inera.intyg.insuranceprocess.healthreporting.getcertificatecontentresponder.v1.GetCertificateContentResponseType

/**
 *
 * @author andreaskaltenbach
 */
public class HamtaIntygsinnehall extends WsClientFixture {

    private GetCertificateContentResponderInterface getCertificateContentResponder

	static String serviceUrl = System.getProperty("service.getCertificateContentUrl")
	
    public HamtaIntygsinnehall() {
		String url = serviceUrl ? serviceUrl : baseUrl + "get-certificate-content/v1.0"
		getCertificateContentResponder = createClient(GetCertificateContentResponderInterface.class, url)
    }

    String personnummer
    String intyg
	String förväntatSvar
	private String faktisktSvar
	private String resultat

    GetCertificateContentResponseType response

    public void execute() {
		faktisktSvar = null
        GetCertificateContentRequestType request = new GetCertificateContentRequestType()
        request.setNationalIdentityNumber(personnummer)
        request.setCertificateId(intyg)

        response = getCertificateContentResponder.getCertificateContent(logicalAddress, request)
		switch (response.result.resultCode) {
			case ResultCodeEnum.OK:
				faktisktSvar = response.certificate
				resultat = "OK"
				break
			case ResultCodeEnum.INFO:
				resultat = "[${response.result.resultCode.toString()}] - ${response.result.infoText}"
				break
			default:
				resultat = "[${response.result.resultCode.toString()}] - ${response.result.errorText}"
		}

    }

    public String resultat() {
        if (response.result.resultCode == ResultCodeEnum.OK && förväntatSvar) {
            try {
				JSONAssert.assertEquals(förväntatSvar, faktisktSvar, false)
				return "OK"
			} catch (AssertionError e) {
				asErrorMessage(e.message)
			}
		}
		else resultat
    }

	public String svar() {
		faktisktSvar
	}

    public String status() {
        response.meta.status.collect{it.type.toString()}
    }

}
