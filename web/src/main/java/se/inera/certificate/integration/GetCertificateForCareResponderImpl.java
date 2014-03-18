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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.dom.DOMSource;

import static se.inera.certificate.integration.util.ResultOfCallUtil.okResult;

import com.google.common.base.Throwables;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;
import org.w3c.dom.Document;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.integration.converter.ModelConverter;
import se.inera.certificate.model.dao.Certificate;

/**
 * @author andreaskaltenbach
 */
@Transactional
@SchemaValidation
public class GetCertificateForCareResponderImpl extends AbstractGetCertificateResponderImpl implements
        GetCertificateForCareResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCertificateForCareResponderImpl.class);

    private static Unmarshaller unmarshaller;

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(UtlatandeType.class);
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to initialize JAXB context required for unmarshaller");
        }
    }

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

        Document document = getCertificateDocument(certificate);

        UtlatandeType utlatande = null;
        try {
            utlatande = unmarshaller.unmarshal(new DOMSource(document), UtlatandeType.class).getValue();
        } catch (JAXBException e) {
            LOGGER.error("Failed to unmarshall intyg coming from module " + certificate.getType());
            Throwables.propagate(e);
        }

        response.setCertificate(utlatande);
        response.setResult(okResult());
    }
}
