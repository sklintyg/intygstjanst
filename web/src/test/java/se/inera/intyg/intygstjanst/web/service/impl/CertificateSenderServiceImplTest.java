/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.Amnetyp;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.rivtabp20.v3.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.exception.MissingModuleException;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.exception.SubsystemCallException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;
import se.inera.intyg.intygstjanst.web.support.CertificateFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateSenderServiceImplTest {

    private static final String CERTIFICATE_ID = "123456";
    private static final String CERTIFICATE_TYPE = "fk7263";
    private static final String CERTIFICATE_TYPE_VERSION = "1.0";

    private static final String RECIPIENT_ID = "FKASSA";
    private static final String RECIPIENT_NAME = "Försäkringskassan";
    private static final String RECIPIENT_LOGICALADDRESS = "FKORG";
    private static final String RECIPIENT_DEFAULT_LOGICALADDRESS = "FKORG-DEFAULT";
    private static final String RECIPIENT_CERTIFICATETYPES = "fk7263";
    private static Certificate certificate = CertificateFactory.buildCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
    @Mock
    private RecipientService recipientService;
    @Mock
    private CertificateService certificateService;
    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private ModuleEntryPoint moduleEntryPoint;
    @Mock
    private ModuleApi moduleApi;
    @Mock
    private RegisterMedicalCertificateResponderInterface registerClient;
    @Mock
    private SendMedicalCertificateQuestionResponderInterface sendMedicalCertificateQuestionResponderInterface;
    @Mock
    private RevokeMedicalCertificateResponderInterface revokeMedicalCertificateResponderInterface;
    @Mock
    private MonitoringLogService monitoringLogService;
    @InjectMocks
    private CertificateSenderServiceImpl senderService = new CertificateSenderServiceImpl();

    private static Recipient createRecipient() {
        return new RecipientBuilder()
                .setId(RECIPIENT_ID)
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

    @Before
    public void setupModuleRestApiFactory() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleEntryPoint(anyString())).thenReturn(moduleEntryPoint);
        when(moduleRegistry.getModuleApi(or(isNull(), anyString()), or(isNull(), anyString()))).thenReturn(moduleApi);
        when(moduleEntryPoint.getDefaultRecipient()).thenReturn(RECIPIENT_DEFAULT_LOGICALADDRESS);
    }

    @Before
    public void setupRecipientService() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(createRecipient());
        when(recipientService.listRecipients(any(CertificateType.class))).thenReturn(Arrays.asList(createRecipient()));
        when(recipientService.getPrimaryRecipientFkassa()).thenReturn(createRecipient());
    }

    @Test
    public void testSend() throws Exception {
        senderService.sendCertificate(certificate, RECIPIENT_ID);
        verify(moduleApi).sendCertificateToRecipient(anyString(), eq(RECIPIENT_LOGICALADDRESS), eq(RECIPIENT_ID));
    }

    @Test
    public void testSendWithDefaultRecipient() throws ModuleException {
        senderService.sendCertificate(certificate, null);
        verify(moduleApi).sendCertificateToRecipient(anyString(), eq(RECIPIENT_DEFAULT_LOGICALADDRESS), Mockito.isNull(String.class));
    }

    @Test(expected = ServerException.class)
    public void testSendWithFailingModule() throws Exception {
        // web service call fails
        doThrow(new ModuleException("")).when(moduleApi).sendCertificateToRecipient(anyString(), eq(RECIPIENT_LOGICALADDRESS),
                eq(RECIPIENT_ID));
        senderService.sendCertificate(certificate, RECIPIENT_ID);
    }

    @Test(expected = MissingModuleException.class)
    public void testSendWithModuleNotFound() throws Exception {
        doThrow(new ModuleNotFoundException("")).when(moduleRegistry).getModuleEntryPoint(CERTIFICATE_TYPE);
        senderService.sendCertificate(certificate, RECIPIENT_ID);
    }

    @Test(expected = ServerException.class)
    public void testSendWithUnknownRecipient() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenThrow(new RecipientUnknownException(""));
        senderService.sendCertificate(certificate, RECIPIENT_ID);
    }

    @Test(expected = ServerException.class)
    public void testSendWithNoMatchingRecipient() {
        senderService.sendCertificate(certificate, "TS");
    }

    @Test
    public void sendCertificateRevocationTest() {
        final String meddelande = "anledning till makulering";
        SendMedicalCertificateQuestionResponseType sendMedicalCertificateQuestionResponse = new SendMedicalCertificateQuestionResponseType();
        sendMedicalCertificateQuestionResponse.setResult(ResultOfCallUtil.okResult());
        when(sendMedicalCertificateQuestionResponderInterface.sendMedicalCertificateQuestion(any(AttributedURIType.class),
                any(SendMedicalCertificateQuestionType.class))).thenReturn(sendMedicalCertificateQuestionResponse);
        RevokeType revokeData = new RevokeType();
        revokeData.setLakarutlatande(new LakarutlatandeEnkelType());
        revokeData.setMeddelande(meddelande);
        senderService.sendCertificateRevocation(certificate, RECIPIENT_ID, revokeData);

        verifyZeroInteractions(revokeMedicalCertificateResponderInterface);
        verify(monitoringLogService).logCertificateRevokeSent(anyString(), anyString(), anyString(), anyString());
        ArgumentCaptor<AttributedURIType> uriCaptor = ArgumentCaptor.forClass(AttributedURIType.class);
        ArgumentCaptor<SendMedicalCertificateQuestionType> requestCaptor = ArgumentCaptor
                .forClass(SendMedicalCertificateQuestionType.class);
        verify(sendMedicalCertificateQuestionResponderInterface).sendMedicalCertificateQuestion(uriCaptor.capture(),
                requestCaptor.capture());

        assertEquals(RECIPIENT_LOGICALADDRESS, uriCaptor.getValue().getValue());
        assertEquals(Amnetyp.MAKULERING_AV_LAKARINTYG, requestCaptor.getValue().getQuestion().getAmne());
        assertEquals(meddelande, requestCaptor.getValue().getQuestion().getFraga().getMeddelandeText());
    }

    @Test
    public void sendCertificateRevocationNoMessageTest() {
        SendMedicalCertificateQuestionResponseType sendMedicalCertificateQuestionResponse = new SendMedicalCertificateQuestionResponseType();
        sendMedicalCertificateQuestionResponse.setResult(ResultOfCallUtil.okResult());
        when(sendMedicalCertificateQuestionResponderInterface.sendMedicalCertificateQuestion(any(AttributedURIType.class),
                any(SendMedicalCertificateQuestionType.class))).thenReturn(sendMedicalCertificateQuestionResponse);
        RevokeType revokeData = new RevokeType();
        revokeData.setLakarutlatande(new LakarutlatandeEnkelType());
        senderService.sendCertificateRevocation(certificate, RECIPIENT_ID, revokeData);

        verifyZeroInteractions(revokeMedicalCertificateResponderInterface);
        verify(monitoringLogService).logCertificateRevokeSent(anyString(), anyString(), anyString(), anyString());
        ArgumentCaptor<AttributedURIType> uriCaptor = ArgumentCaptor.forClass(AttributedURIType.class);
        ArgumentCaptor<SendMedicalCertificateQuestionType> requestCaptor = ArgumentCaptor
                .forClass(SendMedicalCertificateQuestionType.class);
        verify(sendMedicalCertificateQuestionResponderInterface).sendMedicalCertificateQuestion(uriCaptor.capture(),
                requestCaptor.capture());

        assertEquals(RECIPIENT_LOGICALADDRESS, uriCaptor.getValue().getValue());
        assertEquals(Amnetyp.MAKULERING_AV_LAKARINTYG, requestCaptor.getValue().getQuestion().getAmne());
        assertEquals("meddelande saknas", requestCaptor.getValue().getQuestion().getFraga().getMeddelandeText());
    }

    @Test(expected = SubsystemCallException.class)
    public void sendCertificateRevocationFkErrorTest() {
        SendMedicalCertificateQuestionResponseType sendMedicalCertificateQuestionResponse = new SendMedicalCertificateQuestionResponseType();
        sendMedicalCertificateQuestionResponse.setResult(ResultOfCallUtil.failResult("error"));
        when(sendMedicalCertificateQuestionResponderInterface.sendMedicalCertificateQuestion(any(AttributedURIType.class),
                any(SendMedicalCertificateQuestionType.class))).thenReturn(sendMedicalCertificateQuestionResponse);
        RevokeType revokeData = new RevokeType();
        revokeData.setLakarutlatande(new LakarutlatandeEnkelType());
        senderService.sendCertificateRevocation(certificate, RECIPIENT_ID, revokeData);
    }

    @Test
    public void sendCertificateRevocationDefaultStrategyTest() throws Exception {
        final String nonFkRecipient = "TS";
        RevokeMedicalCertificateResponseType revokeMedicalCertificateResponse = new RevokeMedicalCertificateResponseType();
        revokeMedicalCertificateResponse.setResult(ResultOfCallUtil.okResult());
        when(revokeMedicalCertificateResponderInterface.revokeMedicalCertificate(any(AttributedURIType.class),
                any(RevokeMedicalCertificateRequestType.class))).thenReturn(revokeMedicalCertificateResponse);
        when(recipientService.getRecipient(nonFkRecipient)).thenReturn(createRecipient());
        RevokeType revokeData = new RevokeType();
        revokeData.setLakarutlatande(new LakarutlatandeEnkelType());
        revokeData.getLakarutlatande().setLakarutlatandeId(CERTIFICATE_ID);
        senderService.sendCertificateRevocation(certificate, nonFkRecipient, revokeData);

        verifyZeroInteractions(sendMedicalCertificateQuestionResponderInterface);
        verify(monitoringLogService).logCertificateRevokeSent(anyString(), anyString(), anyString(), anyString());
        ArgumentCaptor<AttributedURIType> uriCaptor = ArgumentCaptor.forClass(AttributedURIType.class);
        ArgumentCaptor<RevokeMedicalCertificateRequestType> requestCaptor = ArgumentCaptor
                .forClass(RevokeMedicalCertificateRequestType.class);
        verify(revokeMedicalCertificateResponderInterface).revokeMedicalCertificate(uriCaptor.capture(), requestCaptor.capture());

        assertEquals(RECIPIENT_LOGICALADDRESS, uriCaptor.getValue().getValue());
        assertEquals(CERTIFICATE_ID, requestCaptor.getValue().getRevoke().getLakarutlatande().getLakarutlatandeId());
    }

    @Test(expected = SubsystemCallException.class)
    public void sendCertificateRevocationDefaultRecipientErrorTest() throws Exception {
        final String nonFkRecipient = "TS";
        RevokeMedicalCertificateResponseType revokeMedicalCertificateResponse = new RevokeMedicalCertificateResponseType();
        revokeMedicalCertificateResponse.setResult(ResultOfCallUtil.failResult("error"));
        when(revokeMedicalCertificateResponderInterface.revokeMedicalCertificate(any(AttributedURIType.class),
                any(RevokeMedicalCertificateRequestType.class))).thenReturn(revokeMedicalCertificateResponse);
        when(recipientService.getRecipient(nonFkRecipient)).thenReturn(createRecipient());
        RevokeType revokeData = new RevokeType();
        revokeData.setLakarutlatande(new LakarutlatandeEnkelType());
        revokeData.getLakarutlatande().setLakarutlatandeId(CERTIFICATE_ID);
        senderService.sendCertificateRevocation(certificate, nonFkRecipient, revokeData);
    }

    @Test(expected = RuntimeException.class)
    public void sendCertificateRevocationUnknownRecipientTest() throws Exception {
        final String nonFkRecipient = "TS";
        when(recipientService.getRecipient(nonFkRecipient)).thenThrow(new RecipientUnknownException(""));
        RevokeType revokeData = new RevokeType();
        revokeData.setLakarutlatande(new LakarutlatandeEnkelType());
        revokeData.getLakarutlatande().setLakarutlatandeId(CERTIFICATE_ID);
        senderService.sendCertificateRevocation(certificate, nonFkRecipient, revokeData);
    }
}
