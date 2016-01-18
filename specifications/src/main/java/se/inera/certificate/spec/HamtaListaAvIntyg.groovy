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

import org.joda.time.LocalDate
import se.inera.intyg.common.specifications.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.certificate.v1.CertificateMetaType
import se.inera.ifv.insuranceprocess.healthreporting.listcertificates.rivtabp20.v1.ListCertificatesResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.listcertificatesresponder.v1.ListCertificatesRequestType
import se.inera.ifv.insuranceprocess.healthreporting.listcertificatesresponder.v1.ListCertificatesResponseType
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall

public class HamtaListaAvIntyg extends WsClientFixture {

	private ListCertificatesResponderInterface listCertificatesResponder

    static String serviceUrl = System.getProperty("service.listCertificatesUrl")

	public HamtaListaAvIntyg() {
		String url = serviceUrl ? serviceUrl : baseUrl + "list-certificates/v1.0"
		listCertificatesResponder = createClient(ListCertificatesResponderInterface.class, url)
	}

	String personnr
	private String[] typ
	private LocalDate från
	private LocalDate till
	String kommentar

	public void setTyp(String[] array) {
		if (isEmpty(array)) {
			typ = []
		} else {
			typ = array
		}
	}
	public void setFrån(String datum) {
		this.från = LocalDate.parse(datum)
	}

	public void setTill(String datum) {
		this.till = LocalDate.parse(datum)
	}

	private String svar
	private String intyg

    public void execute() {
		svar = null
		intyg = null
		//GetConsentRequestType getConsentParameters = new GetConsentRequestType()
		//getConsentParameters.personnummer = personnr
		//GetConsentResponseType getConsentResponse = getConsentResponder.getConsent(logicalAddress, getConsentParameters)
		//if (!getConsentResponse.consentGiven) {
		//	svar = "samtycke saknas"
		//	return
		//}
		ListCertificatesRequestType parameters = new ListCertificatesRequestType();
		parameters.nationalIdentityNumber = personnr
		parameters.fromDate = från
		parameters.toDate = till
		if (typ) {
			parameters.certificateType = typ
		}
		ListCertificatesResponseType response = listCertificatesResponder.listCertificates(logicalAddress, parameters)
		ResultOfCall result = response.result
		if (result.resultCode == ResultCodeEnum.OK) {
			def fk_intygs_id = []
			def fk_intyg = []
			List allaIntyg = response.meta
			allaIntyg.each {CertificateMetaType metaType ->
				fk_intygs_id << metaType.certificateId
				fk_intyg << metaType.certificateId + ":" + metaType.certificateType + ":" + metaType.signDate + ":" + metaType.validFrom + ":" + metaType.validTo + ":" + metaType.issuerName + ":" + metaType.facilityName
 			}
			svar = fk_intygs_id.sort().toString()
			intyg = fk_intyg.sort().toString()
		} else if (result.resultCode == ResultCodeEnum.INFO) {
			svar = result.infoText
		} else {
			svar = result.errorText
		}
	}

	public String svar() {
		svar
	}

	public String intyg() {
		intyg
	}

	private boolean isEmpty(String[] array) {
		switch (array.length) {
			case 0: return true;
			case 1: return array[0].isEmpty();
			default: return false;
		}
	}

}
