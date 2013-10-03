/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate (http://code.google.com/p/inera-certificate).
 *
 * Inera Certificate is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Certificate is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.certificate.integration;

import static se.inera.certificate.integration.util.ResultOfCallUtil.okResult;

import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;
import org.w3c.dom.Element;

import se.inera.certificate.integration.converter.ModelConverter;
import se.inera.certificate.model.dao.Certificate;
import se.inera.ifv.insuranceprocess.healthreporting.vardgetcertificate.v1.rivtabp20.GetCertificateForCareResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.vardgetcertificateresponder.v1.CertificateType;
import se.inera.ifv.insuranceprocess.healthreporting.vardgetcertificateresponder.v1.GetCertificateForCareRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.vardgetcertificateresponder.v1.GetCertificateForCareResponseType;

/**
 * @author andreaskaltenbach
 */
@Transactional
@SchemaValidation
public class GetCertificateForCareResponderImpl extends AbstractGetCertificateResponderImpl implements
        GetCertificateForCareResponderInterface {

    @Override
    public GetCertificateForCareResponseType getCertificateForCare(AttributedURIType logicalAddress,
            GetCertificateForCareRequestType request) {
        GetCertificateForCareResponseType response = new GetCertificateForCareResponseType();

        CertificateOrResultOfCall certificateOrResultOfCall = getCertificate(request.getCertificateId(), null);

        if (certificateOrResultOfCall.hasError()) {
            response.setResult(certificateOrResultOfCall.getResultOfCall());
            return response;
        }

        Certificate certificate = certificateOrResultOfCall.getCertificate();
        response.setMeta(ModelConverter.toCertificateMetaType(certificate));
        attachCertificateDocument(certificate, response);
        return response;
    }

    @Override
    protected String getMarshallVersion() {
        return "2.0";
    }

    protected void attachCertificateDocument(Certificate certificate, GetCertificateForCareResponseType response) {
        Element documentElement = getCertificateDocument(certificate);
        CertificateType certificateType = new CertificateType();
        certificateType.getAny().add(documentElement);

        response.setCertificate(certificateType);
        response.setResult(okResult());
    }
}
