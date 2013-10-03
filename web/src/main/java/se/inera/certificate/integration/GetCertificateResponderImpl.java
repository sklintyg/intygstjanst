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
import se.inera.ifv.insuranceprocess.healthreporting.getcertificate.v1.rivtabp20.GetCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateresponder.v1.CertificateType;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateresponder.v1.GetCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateresponder.v1.GetCertificateResponseType;

/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class GetCertificateResponderImpl extends AbstractGetCertificateResponderImpl implements
        GetCertificateResponderInterface {

    @Override
    public GetCertificateResponseType getCertificate(AttributedURIType logicalAddress, GetCertificateRequestType request) {
        GetCertificateResponseType response = new GetCertificateResponseType();

        CertificateOrResultOfCall certificateOrResultOfCall = getCertificate(request.getCertificateId(),
                request.getNationalIdentityNumber());

        if (certificateOrResultOfCall.hasError()) {
            response.setResult(certificateOrResultOfCall.getResultOfCall());
            return response;
        }

        Certificate certificate = certificateOrResultOfCall.getCertificate();
        response.setMeta(ModelConverter.toCertificateMetaType(certificate));
        attachCertificateDocument(certificate, response);
        return response;
    }

    protected void attachCertificateDocument(Certificate certificate, GetCertificateResponseType response) {
        Element documentElement = getCertificateDocument(certificate);
        CertificateType certificateType = new CertificateType();
        certificateType.getAny().add(documentElement);

        response.setCertificate(certificateType);
        response.setResult(okResult());
    }

    @Override
    protected String getMarshallVersion() {
        return "1.0";
    }
}
