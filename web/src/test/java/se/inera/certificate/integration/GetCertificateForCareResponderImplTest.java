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

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import java.io.ByteArrayOutputStream;

import static junit.framework.Assert.assertNull;
import static org.custommonkey.xmlunit.DifferenceConstants.NAMESPACE_PREFIX_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.ERROR;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.INFO;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.OK;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Node;
import se.inera.certificate.exception.CertificateRevokedException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.model.Kod;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.builder.CertificateBuilder;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.vardgetcertificateresponder.v1.GetCertificateForCareRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.vardgetcertificateresponder.v1.GetCertificateForCareResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.vardgetcertificateresponder.v1.ObjectFactory;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class GetCertificateForCareResponderImplTest {

    private static final String CERTIFICATE_ID = "123456";
    private static final String CERTIFICATE_TYPE = "fk7263";
    private static final String CERTIFICATE_DATA = "<intyg>";

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @InjectMocks
    private GetCertificateForCareResponderImpl responder = new GetCertificateForCareResponderImpl();

    @Mock
    private ModuleRestApiFactory moduleRestApiFactory = mock(ModuleRestApiFactory.class);

    @Mock
    private ModuleRestApi moduleRestApi = mock(ModuleRestApi.class);

    @Mock
    private Response restResponse = mock(Response.class);

    @Test
    public void getCertificateForCare() throws Exception {
        Utlatande utlatande = new Utlatande();
        utlatande.setTyp(new Kod(CERTIFICATE_TYPE));

        when(certificateService.getCertificate(null, CERTIFICATE_ID)).thenReturn(
                new CertificateBuilder(CERTIFICATE_ID, CERTIFICATE_DATA).certificateType(CERTIFICATE_TYPE).build());

        when(certificateService.getLakarutlatande(any(Certificate.class))).thenReturn(utlatande);

        when(moduleRestApiFactory.getModuleRestService("fk7263")).thenReturn(moduleRestApi);
        when(restResponse.getEntity()).thenReturn(
                new ClassPathResource("GetCertificateForCareResponderImplTest/utlatande.xml").getInputStream());
        when(moduleRestApi.marshall("2.0", CERTIFICATE_DATA)).thenReturn(restResponse);

        GetCertificateForCareRequestType request = createGetCertificateForCareRequest(CERTIFICATE_ID);
        GetCertificateForCareResponseType response = responder.getCertificateForCare(null, request);

        verify(certificateService).getCertificate(null, CERTIFICATE_ID);

        assertNotNull(response.getMeta());
        assertEquals(OK, response.getResult().getResultCode());

        // compare response XML with reference response XML
        compareResponseWithReferenceFile(response, "GetCertificateForCareResponderImplTest/response.xml");
    }

    private void compareResponseWithReferenceFile(GetCertificateForCareResponseType response, String fileName)
            throws Exception {
        ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
        JAXBElement<GetCertificateForCareResponseType> jaxbElement = new ObjectFactory()
                .createGetCertificateForCareResponse(response);
        JAXBContext context = JAXBContext.newInstance(GetCertificateForCareResponseType.class);
        context.createMarshaller().marshal(jaxbElement, byteArr);

        String referenceXml = FileUtils.readFileToString(new ClassPathResource(fileName).getFile());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);
        Diff diff = new Diff(referenceXml, new String(byteArr.toByteArray()));
        diff.overrideDifferenceListener(new NamespacePrefixNameIgnoringListener());

        assertTrue(diff.toString(), diff.identical());
    }

    private class NamespacePrefixNameIgnoringListener implements DifferenceListener {
        public int differenceFound(Difference difference) {
            if (NAMESPACE_PREFIX_ID == difference.getId()) {
                // differences in namespace prefix IDs are ok (eg. 'ns1' vs 'ns2'), as long as the namespace URI is the
                // same
                return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            } else {
                return RETURN_ACCEPT_DIFFERENCE;
            }
        }

        public void skippedComparison(Node control, Node test) {
        }
    }

    @Test
    public void getCertificateForCareWithUnknownCertificateId() {

        when(certificateService.getCertificate(null, CERTIFICATE_ID)).thenThrow(
                new InvalidCertificateException("123456", null));

        GetCertificateForCareRequestType parameters = createGetCertificateForCareRequest(CERTIFICATE_ID);

        GetCertificateForCareResponseType response = responder.getCertificateForCare(null, parameters);

        assertNull(response.getMeta());
        assertNull(response.getCertificate());
        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Unknown certificate ID: 123456", response.getResult().getErrorText());
    }

    @Test
    public void getRevokedCertificate() {

        when(certificateService.getCertificate(null, CERTIFICATE_ID)).thenThrow(
                new CertificateRevokedException("123456"));

        GetCertificateForCareRequestType parameters = createGetCertificateForCareRequest(CERTIFICATE_ID);

        GetCertificateForCareResponseType response = responder.getCertificateForCare(null, parameters);

        assertNull(response.getMeta());
        assertNull(response.getCertificate());
        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("Certificate '123456' has been revoked", response.getResult().getInfoText());
    }

    private GetCertificateForCareRequestType createGetCertificateForCareRequest(String certificateId) {
        GetCertificateForCareRequestType parameters = new GetCertificateForCareRequestType();
        parameters.setCertificateId(certificateId);
        return parameters;
    }
}
