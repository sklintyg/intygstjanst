/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceProvider;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3._2000._09.xmldsig_.SignatureType;
import org.w3._2002._06.xmldsig_filter2.XPathType;
import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.common.support.stub.MedicalCertificatesStore;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.ObjectFactory;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.DatePeriodType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PQType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateType;

@SchemaValidation
@WebServiceProvider(targetNamespace = "urn:riv:clinicalprocess:healthcond:certificate:RevokeCertificateResponder:1")
public class RevokeCertificateResponderStub implements RevokeCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeCertificateResponderStub.class);

    @Autowired
    private MedicalCertificatesStore store;

    @Override
    public RevokeCertificateResponseType revokeCertificate(String logicalAddress, RevokeCertificateType request) {
        RevokeCertificateResponseType response = new RevokeCertificateResponseType();

        String id = request.getIntygsId().getExtension();
        String meddelande = request.getMeddelande();

        LOGGER.info("STUB Received revocation concerning certificate with id: " + id);
        store.makulera(id, meddelande);

        response.setResult(ResultTypeUtil.okResult());

        try {
            final var out = new PrintWriter(id + ".xml");
            out.write(xmlToString(request));
            out.close();
        } catch (Exception e) {
            return null;
        }

        return response;
    }

    private String xmlToString(RevokeCertificateType type) throws JAXBException {
        final var jaxbContext = JAXBContext.newInstance(RevokeCertificateType.class, DatePeriodType.class, SignatureType.class,
            XPathType.class, PartialDateType.class, PQType.class);

        StringWriter stringWriter = new StringWriter();
        JAXBElement<RevokeCertificateType> requestElement = new ObjectFactory().createRevokeCertificate(type);
        jaxbContext.createMarshaller().marshal(requestElement, stringWriter);
        return stringWriter.toString();
    }

}
