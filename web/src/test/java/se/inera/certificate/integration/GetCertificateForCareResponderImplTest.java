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

import static junit.framework.Assert.assertNull;
import static org.custommonkey.xmlunit.DifferenceConstants.NAMESPACE_PREFIX_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType.OK;
import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType.ERROR;
import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType.VALIDATION_ERROR;
import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType.REVOKED;
import static se.inera.certificate.modules.support.api.dto.TransportModelVersion.UTLATANDE_V1;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Node;

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.ObjectFactory;
import se.inera.certificate.exception.ClientException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.integration.converter.MetaDataResolver;
import se.inera.certificate.integration.module.ModuleApiFactory;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.Kod;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.builder.CertificateBuilder;
import se.inera.certificate.model.common.MinimalUtlatande;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.TransportModelResponse;
import se.inera.certificate.service.CertificateService;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class GetCertificateForCareResponderImplTest {

    private static final String CERTIFICATE_ID = "123456";
    private static final String CERTIFICATE_TYPE = "fk7263";
    private static final ExternalModelHolder CERTIFICATE_DATA = new ExternalModelHolder("<intyg>");
    private static final String CERTIFICATE_COMPLEMENATARY_INFO = "DATA FROM MODULE";

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @InjectMocks
    private GetCertificateForCareResponderImpl responder = new GetCertificateForCareResponderImpl();

    @Mock
    private ModuleApiFactory moduleApiFactory = mock(ModuleApiFactory.class);

    @Mock
    private ModuleEntryPoint moduleEntryPoint = mock(ModuleEntryPoint.class);

    @Mock
    private ModuleApi moduleRestApi = mock(ModuleApi.class);

    @Spy
    @InjectMocks
    private MetaDataResolver metaDataResolver = new MetaDataResolver();

    @Test
    public void getCertificateForCare() throws Exception {
        Utlatande utlatande = new MinimalUtlatande();
        utlatande.setTyp(new Kod(CERTIFICATE_TYPE));

        when(certificateService.getCertificate(CERTIFICATE_ID)).thenReturn(
                new CertificateBuilder(CERTIFICATE_ID, CERTIFICATE_DATA.getExternalModel()).certificateType(CERTIFICATE_TYPE)
                        .validity("2013-10-01", "2013-10-03").signedDate(new LocalDateTime("2013-10-03")).build());

        when(certificateService.getLakarutlatande(any(Certificate.class))).thenReturn(utlatande);

        when(moduleApiFactory.getModuleEntryPoint("fk7263")).thenReturn(moduleEntryPoint);
        when(moduleEntryPoint.getModuleApi()).thenReturn(moduleRestApi);
        TransportModelResponse marshallResponse = new TransportModelResponse(IOUtils.toString(new ClassPathResource(
                "GetCertificateForCareResponderImplTest/utlatande.xml").getInputStream()));
        when(moduleRestApi.marshall(any(ExternalModelHolder.class), eq(UTLATANDE_V1))).thenReturn(marshallResponse);
        when(moduleRestApi.getComplementaryInfo(any(ExternalModelHolder.class))).thenReturn(CERTIFICATE_COMPLEMENATARY_INFO);

        GetCertificateForCareRequestType request = createGetCertificateForCareRequest(CERTIFICATE_ID);
        GetCertificateForCareResponseType response = responder.getCertificateForCare(null, request);

        verify(certificateService).getCertificate(CERTIFICATE_ID);

        assertNotNull(response.getMeta());
        assertEquals(OK, response.getResult().getResultCode());

        // compare response XML with reference response XML
        compareResponseWithReferenceFile(response, "GetCertificateForCareResponderImplTest/response.xml");
    }

    private void compareResponseWithReferenceFile(GetCertificateForCareResponseType response, String fileName)
            throws Exception {
        ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
        JAXBContext context = JAXBContext.newInstance(GetCertificateForCareResponseType.class);
        JAXBElement<GetCertificateForCareResponseType> responseElement = new ObjectFactory()
                .createGetCertificateForCareResponse(response);
        context.createMarshaller().marshal(responseElement, byteArr);

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
    public void getCertificateForCareWithUnknownCertificateId() throws ClientException {

        when(certificateService.getCertificate(CERTIFICATE_ID)).thenThrow(
                new InvalidCertificateException("123456", null));

        GetCertificateForCareRequestType parameters = createGetCertificateForCareRequest(CERTIFICATE_ID);

        GetCertificateForCareResponseType response = responder.getCertificateForCare(null, parameters);

        assertNull(response.getMeta());
        assertNull(response.getCertificate());
        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Unknown certificate ID: 123456", response.getResult().getResultText());
    }

    @Test
    public void getRevokedCertificate() throws Exception {
        Utlatande utlatande = new MinimalUtlatande();
        utlatande.setTyp(new Kod(CERTIFICATE_TYPE));

        when(certificateService.getCertificate(CERTIFICATE_ID)).thenReturn(
                new CertificateBuilder(CERTIFICATE_ID, CERTIFICATE_DATA.getExternalModel()).certificateType(CERTIFICATE_TYPE)
                        .validity("2013-10-01", "2013-10-03").signedDate(new LocalDateTime("2013-10-03")).state(CertificateState.CANCELLED, "FK").build());

        when(certificateService.getLakarutlatande(any(Certificate.class))).thenReturn(utlatande);

        when(moduleApiFactory.getModuleEntryPoint("fk7263")).thenReturn(moduleEntryPoint);
        when(moduleEntryPoint.getModuleApi()).thenReturn(moduleRestApi);
        TransportModelResponse marshallResponse = new TransportModelResponse(IOUtils.toString(new ClassPathResource(
                "GetCertificateForCareResponderImplTest/utlatande.xml").getInputStream()));
        when(moduleRestApi.marshall(any(ExternalModelHolder.class), eq(UTLATANDE_V1))).thenReturn(marshallResponse);

        GetCertificateForCareRequestType parameters = createGetCertificateForCareRequest(CERTIFICATE_ID);

        GetCertificateForCareResponseType response = responder.getCertificateForCare(null, parameters);

        assertNotNull(response.getMeta());
        assertNotNull(response.getCertificate());
        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(REVOKED, response.getResult().getErrorId());
        assertEquals("Certificate '" + CERTIFICATE_ID  + "' has been revoked", response.getResult().getResultText());
    }

    private GetCertificateForCareRequestType createGetCertificateForCareRequest(String certificateId) {
        GetCertificateForCareRequestType parameters = new GetCertificateForCareRequestType();
        parameters.setCertificateId(certificateId);
        return parameters;
    }
}
