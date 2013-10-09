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
import java.io.IOException;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.ERROR;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.INFO;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.OK;

import org.apache.commons.io.FileUtils;
import org.h2.util.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.certificate.exception.CertificateRevokedException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.exception.MissingConsentException;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.builder.CertificateBuilder;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateresponder.v1.GetCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateresponder.v1.GetCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class GetCertificateResponderImplTest {

    private static final String civicRegistrationNumber = "19350108-1234";
    private static final String certificateId = "123456";

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @InjectMocks
    private GetCertificateResponderImpl responder = new GetCertificateResponderImpl();

    @Mock
    private ModuleRestApiFactory moduleRestApiFactory = mock(ModuleRestApiFactory.class);

    @Mock
    private ModuleRestApi moduleRestApi = mock(ModuleRestApi.class);

    @Mock
    private Response restResponse = mock(Response.class);

    @Test
    public void getCertificate() throws IOException {
        String document = FileUtils.readFileToString(new ClassPathResource("lakarutlatande/maximalt-fk7263.json").getFile());
        Utlatande utlatande = new CustomObjectMapper().readValue(document, Utlatande.class);

        when(certificateService.getCertificate(civicRegistrationNumber, certificateId)).thenReturn(
                new CertificateBuilder("123456", document).certificateType("fk7263").build());

        when(certificateService.getLakarutlatande(any(Certificate.class))).thenReturn(utlatande);

        when(moduleRestApiFactory.getModuleRestService("fk7263")).thenReturn(moduleRestApi);
        when(restResponse.getEntity()).thenReturn(IOUtils.getInputStreamFromString("<someXml></someXml>"));
        when(moduleRestApi.marshall("1.0", document)).thenReturn(restResponse);

        GetCertificateRequestType parameters = createGetCertificateRequest(civicRegistrationNumber, certificateId);

        GetCertificateResponseType response = responder.getCertificate(null, parameters);

        verify(certificateService).getCertificate(civicRegistrationNumber, certificateId);

        assertNotNull(response.getMeta());
        assertEquals(OK, response.getResult().getResultCode());
    }

    @Test
    public void getCertificateWithUnknownCertificateId() {

        when(certificateService.getCertificate(civicRegistrationNumber, certificateId)).thenThrow(new InvalidCertificateException("123456", null));

        GetCertificateRequestType parameters = createGetCertificateRequest(civicRegistrationNumber, certificateId);

        GetCertificateResponseType response = responder.getCertificate(null, parameters);

        assertNull(response.getMeta());
        assertNull(response.getCertificate());
        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Unknown certificate ID: 123456", response.getResult().getErrorText());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getCertificateWithoutConsent() {

        when(certificateService.getCertificate(civicRegistrationNumber, certificateId)).thenThrow(MissingConsentException.class);

        GetCertificateRequestType parameters = createGetCertificateRequest(civicRegistrationNumber, certificateId);

        GetCertificateResponseType response = responder.getCertificate(null, parameters);

        assertNull(response.getMeta());
        assertNull(response.getCertificate());
        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
    }

    @Test
    public void getRevokedCertificate() {

        when(certificateService.getCertificate(civicRegistrationNumber, certificateId)).thenThrow(new CertificateRevokedException("123456"));

        GetCertificateRequestType parameters = createGetCertificateRequest(civicRegistrationNumber, certificateId);

        GetCertificateResponseType response = responder.getCertificate(null, parameters);

        assertNull(response.getMeta());
        assertNull(response.getCertificate());
        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("Certificate '123456' has been revoked", response.getResult().getInfoText());
    }

    private GetCertificateRequestType createGetCertificateRequest(String civicRegistrationNumber, String certificateId) {
        GetCertificateRequestType parameters = new GetCertificateRequestType();
        parameters.setNationalIdentityNumber(civicRegistrationNumber);
        parameters.setCertificateId(certificateId);
        return parameters;
    }
}
