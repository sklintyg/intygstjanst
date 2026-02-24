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
package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.IOException;
import java.util.Collections;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.exception.MissingModuleException;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.exception.SubsystemCallException;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.SoapIntegrationService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;
import se.inera.intyg.intygstjanst.web.support.CertificateFactory;

/**
 * @author andreaskaltenbach
 */
@ExtendWith(MockitoExtension.class)
class CertificateSenderServiceImplTest {

    private static final String CERTIFICATE_ID = "123456";
    private static final String CERTIFICATE_TYPE = "fk7263";
    private static final String CERTIFICATE_TYPE_VERSION = "1.0";

    private static final String RECIPIENT_ID_FKASSA = "FKASSA";
    private static final String RECIPIENT_ID_TRANSP = "TRANSP";
    private static final String RECIPIENT_NAME = "Försäkringskassan";
    private static final String RECIPIENT_LOGICALADDRESS = "FKORG";
    private static final String RECIPIENT_DEFAULT_LOGICALADDRESS = "FKORG-DEFAULT";
    private static final String RECIPIENT_CERTIFICATETYPES = "fk7263";
    private static final Certificate certificate = CertificateFactory
        .buildCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
    @Mock
    private RecipientService recipientService;
    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private ModuleEntryPoint moduleEntryPoint;
    @Mock
    private SoapIntegrationService soapIntegrationService;
    @Mock
    private MonitoringLogService monitoringLogService;
    @InjectMocks
    private CertificateSenderServiceImpl senderService = new CertificateSenderServiceImpl();

    private static Recipient createRecipient() {
        return new RecipientBuilder()
            .setId(RECIPIENT_ID_FKASSA)
            .setName(RECIPIENT_NAME)
            .setLogicalAddress(RECIPIENT_LOGICALADDRESS)
            .setCertificateTypes(RECIPIENT_CERTIFICATETYPES)
            .setActive(true)
            .setTrusted(true)
            .build();
    }

    public RegisterMedicalCertificateType request() throws IOException, JAXBException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(RegisterMedicalCertificateResponseType.class).createUnmarshaller();
        return unmarshaller
            .unmarshal(new StreamSource(new ClassPathResource("CertificateSenderServiceImplTest/utlatande.xml").getInputStream()),
                RegisterMedicalCertificateType.class)
            .getValue();
    }

    @BeforeEach
    void setupModuleRestApiFactory() throws ModuleNotFoundException {
        lenient().when(moduleRegistry.getModuleEntryPoint(anyString())).thenReturn(moduleEntryPoint);
        lenient().when(moduleEntryPoint.getDefaultRecipient()).thenReturn(RECIPIENT_DEFAULT_LOGICALADDRESS);
    }

    @BeforeEach
    void setupRecipientService() throws RecipientUnknownException {
        lenient().when(recipientService.getRecipient(RECIPIENT_ID_FKASSA)).thenReturn(createRecipient());
        lenient().when(recipientService.listRecipients(any(CertificateType.class)))
            .thenReturn(Collections.singletonList(createRecipient()));
    }

    @Test
    void testSend() throws Exception {
        senderService.sendCertificate(certificate, RECIPIENT_ID_FKASSA);
        verify(soapIntegrationService).sendCertificateToRecipient(certificate, RECIPIENT_LOGICALADDRESS, RECIPIENT_ID_FKASSA);
    }

    @Test
    void testSendWithDefaultRecipient() throws ModuleException, ModuleNotFoundException {
        senderService.sendCertificate(certificate, null);
        verify(soapIntegrationService).sendCertificateToRecipient(eq(certificate), eq(RECIPIENT_DEFAULT_LOGICALADDRESS), Mockito.isNull());
    }

    @Test
    void testSendWithFailingModule() throws Exception {
        // web service call fails
        doThrow(new ModuleException("")).when(soapIntegrationService).sendCertificateToRecipient(certificate, RECIPIENT_LOGICALADDRESS,
            RECIPIENT_ID_FKASSA);
        assertThrows(ServerException.class, () -> senderService.sendCertificate(certificate, RECIPIENT_ID_FKASSA));
    }

    @Test
    void testSendWithModuleNotFound() throws Exception {
        doThrow(new ModuleNotFoundException("")).when(moduleRegistry).getModuleEntryPoint(CERTIFICATE_TYPE);
        assertThrows(MissingModuleException.class, () -> senderService.sendCertificate(certificate, RECIPIENT_ID_FKASSA));
    }

    @Test
    void testSendWithUnknownRecipient() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID_FKASSA)).thenThrow(new RecipientUnknownException(""));
        assertThrows(ServerException.class, () -> senderService.sendCertificate(certificate, RECIPIENT_ID_FKASSA));
    }

    @Test
    void testSendWithNoMatchingRecipient() {
        assertThrows(ServerException.class, () -> senderService.sendCertificate(certificate, RECIPIENT_ID_TRANSP));
    }

    @Test
    void sendCertificateRevocationTest() throws Exception {
        final String nonFkRecipient = RECIPIENT_ID_TRANSP;
        RevokeMedicalCertificateResponseType revokeMedicalCertificateResponse = new RevokeMedicalCertificateResponseType();
        revokeMedicalCertificateResponse.setResult(ResultOfCallUtil.okResult());
        when(soapIntegrationService.revokeMedicalCertificate(any(AttributedURIType.class),
            any(RevokeMedicalCertificateRequestType.class))).thenReturn(revokeMedicalCertificateResponse);
        when(recipientService.getRecipient(nonFkRecipient)).thenReturn(createRecipient());
        RevokeType revokeData = new RevokeType();
        revokeData.setLakarutlatande(new LakarutlatandeEnkelType());
        revokeData.getLakarutlatande().setLakarutlatandeId(CERTIFICATE_ID);
        senderService.sendCertificateRevocation(certificate, nonFkRecipient, revokeData);

        verify(monitoringLogService).logCertificateRevokeSent(anyString(), anyString(), anyString(), anyString());
        ArgumentCaptor<AttributedURIType> uriCaptor = ArgumentCaptor.forClass(AttributedURIType.class);
        ArgumentCaptor<RevokeMedicalCertificateRequestType> requestCaptor = ArgumentCaptor
            .forClass(RevokeMedicalCertificateRequestType.class);
        verify(soapIntegrationService).revokeMedicalCertificate(uriCaptor.capture(), requestCaptor.capture());

        assertEquals(RECIPIENT_LOGICALADDRESS, uriCaptor.getValue().getValue());
        assertEquals(CERTIFICATE_ID, requestCaptor.getValue().getRevoke().getLakarutlatande().getLakarutlatandeId());
    }

    @Test
    void sendCertificateRevocationRecipientErrorTest() throws Exception {
        final String nonFkRecipient = RECIPIENT_ID_TRANSP;
        RevokeMedicalCertificateResponseType revokeMedicalCertificateResponse = new RevokeMedicalCertificateResponseType();
        revokeMedicalCertificateResponse.setResult(ResultOfCallUtil.failResult("error"));
        when(soapIntegrationService.revokeMedicalCertificate(any(AttributedURIType.class),
            any(RevokeMedicalCertificateRequestType.class))).thenReturn(revokeMedicalCertificateResponse);
        when(recipientService.getRecipient(nonFkRecipient)).thenReturn(createRecipient());
        RevokeType revokeData = new RevokeType();
        revokeData.setLakarutlatande(new LakarutlatandeEnkelType());
        revokeData.getLakarutlatande().setLakarutlatandeId(CERTIFICATE_ID);
        assertThrows(SubsystemCallException.class, () -> senderService.sendCertificateRevocation(certificate, nonFkRecipient, revokeData));
    }

    @Test
    void sendCertificateRevocationUnknownRecipientTest() throws Exception {
        final String nonFkRecipient = RECIPIENT_ID_TRANSP;
        when(recipientService.getRecipient(nonFkRecipient)).thenThrow(new RecipientUnknownException(""));
        RevokeType revokeData = new RevokeType();
        revokeData.setLakarutlatande(new LakarutlatandeEnkelType());
        revokeData.getLakarutlatande().setLakarutlatandeId(CERTIFICATE_ID);
        assertThrows(RuntimeException.class, () -> senderService.sendCertificateRevocation(certificate, nonFkRecipient, revokeData));
    }
}
