/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration.stub;

import jakarta.xml.ws.WebServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.stub.MedicalCertificatesStore;
import se.inera.intyg.common.support.validate.CertificateValidationException;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.integration.validator.RevokeRequestValidator;

@Transactional
@WebServiceProvider(
    targetNamespace = "urn:riv:insuranceprocess:healthreporting:RevokeMedicalCertificate:1:rivtabp20",
    serviceName = "RevokeMedicalCertificateResponderService",
    wsdlLocation = "classpath:interactions/RevokeMedicalCertificateInteraction/RevokeMedicalCertificateInteraction_1.0_rivtabp20.wsdl"
)
public class RevokeMedicalCertificateResponderStub implements RevokeMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeMedicalCertificateResponderStub.class);

    @Autowired
    private MedicalCertificatesStore store;

    @Override
    @PrometheusTimeMethod
    public RevokeMedicalCertificateResponseType revokeMedicalCertificate(AttributedURIType logicalAddress,
        RevokeMedicalCertificateRequestType request) {

        RevokeMedicalCertificateResponseType response = new RevokeMedicalCertificateResponseType();

        try {
            new RevokeRequestValidator(request.getRevoke()).validateAndCorrect();
            String id = request.getRevoke().getLakarutlatande().getLakarutlatandeId();
            String meddelande = request.getRevoke().getMeddelande();

            LOGGER.info("STUB Received revocation concerning certificate with id: " + id);
            store.makulera(id, meddelande);

        } catch (CertificateValidationException e) {
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            return response;
        }

        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }
}
