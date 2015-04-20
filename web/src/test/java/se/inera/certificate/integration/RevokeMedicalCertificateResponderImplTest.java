package se.inera.certificate.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.exception.PersistenceException;
import se.inera.certificate.exception.SubsystemCallException;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateDao;
import se.inera.certificate.model.dao.CertificateStateHistoryEntry;
import se.inera.certificate.service.CertificateSenderService;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.StatisticsService;
import se.inera.certificate.service.impl.CertificateServiceImpl;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtab20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtab20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.util.Collections;


@RunWith(MockitoJUnitRunner.class)
public class RevokeMedicalCertificateResponderImplTest {

    protected static final String CERTIFICATE_ID = "intygs-id-1234567890";
    protected static final String PERSONNUMMER = "19121212-1212";
    protected static final String TARGET = "FK";

    protected static final AttributedURIType ADDRESS = new AttributedURIType();

    @Mock
    protected CertificateSenderService certificateSenderService;

    @Mock
    protected CertificateDao certificateDao;

    @Spy
    @InjectMocks
    protected CertificateService certificateService = new CertificateServiceImpl();

    @Mock
    protected StatisticsService statisticsService = mock(StatisticsService.class);

    @Mock
    protected SendMedicalCertificateQuestionResponderInterface sendMedicalCertificateQuestionResponderInterface;

    @InjectMocks
    protected RevokeMedicalCertificateResponderInterface responder = createResponder();

    protected RevokeMedicalCertificateResponderInterface createResponder() {
        return new RevokeMedicalCertificateResponderImpl();
    }

    protected RevokeMedicalCertificateRequestType revokeRequest() throws Exception {
        // read request from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RevokeMedicalCertificateRequestType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<RevokeMedicalCertificateRequestType> request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("revoke-medical-certificate/revoke-medical-certificate-request.xml").getInputStream()), RevokeMedicalCertificateRequestType.class);
        return request.getValue();
    }

    private SendMedicalCertificateQuestionType expectedSendRequest() throws Exception {
        // read request from file
        JAXBContext jaxbContext = JAXBContext.newInstance(SendMedicalCertificateQuestionType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<SendMedicalCertificateQuestionType> request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("revoke-medical-certificate/send-medical-certificate-question-request.xml").getInputStream()), SendMedicalCertificateQuestionType.class);
        return request.getValue();
    }

    @Test
    public void testRevokeCertificateWhichWasAlreadySentToForsakringskassan() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID, "text");
        CertificateStateHistoryEntry historyEntry = new CertificateStateHistoryEntry(TARGET, CertificateState.SENT, new LocalDateTime());
        certificate.setStates(Collections.singletonList(historyEntry));

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);
        SendMedicalCertificateQuestionResponseType sendQuestionResponse = new SendMedicalCertificateQuestionResponseType();
        sendQuestionResponse.setResult(ResultOfCallUtil.okResult());
        when(sendMedicalCertificateQuestionResponderInterface.sendMedicalCertificateQuestion(ADDRESS, expectedSendRequest())).thenReturn(sendQuestionResponse);

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        verify(certificateSenderService).sendCertificateRevocation(certificate, TARGET, revokeRequest().getRevoke());

        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
        Mockito.verify(statisticsService, Mockito.only()).revoked(certificate);
    }

    @Test
    public void testRevokeCertificateWithForsakringskassanReturningError() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID, "text");
        CertificateStateHistoryEntry historyEntry = new CertificateStateHistoryEntry(TARGET, CertificateState.SENT, new LocalDateTime());
        certificate.setStates(Collections.singletonList(historyEntry));

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);
        doThrow(new SubsystemCallException(TARGET)).when(certificateSenderService).sendCertificateRevocation(certificate, TARGET,
                revokeRequest().getRevoke());

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());
        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        Mockito.verifyZeroInteractions(statisticsService);
    }

    @Test
    public void testRevokeCertificateWhichWasNotSentToForsakringskassan() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID, "text");

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
        Mockito.verify(statisticsService, Mockito.only()).revoked(certificate);
    }

    @Test
    public void testRevokeUnknownCertificate() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenThrow(new PersistenceException("certificateId", "civicRegistrationNumber"));

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals("No certificate 'intygs-id-1234567890' found to revoke for patient '19121212-1212'.", response.getResult().getErrorText());
        Mockito.verifyZeroInteractions(statisticsService);
    }

    @Test
    public void testRevokeAlreadyRevokedCertificate() throws Exception {
        Certificate certificate = new Certificate(CERTIFICATE_ID, "text");
        CertificateStateHistoryEntry historyEntry = new CertificateStateHistoryEntry(TARGET, CertificateState.CANCELLED, new LocalDateTime());
        certificate.setStates(Collections.singletonList(historyEntry));

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        assertEquals(ResultCodeEnum.INFO, response.getResult().getResultCode());
        assertEquals("Certificate 'intygs-id-1234567890' is already revoked.", response.getResult().getInfoText());
        Mockito.verifyZeroInteractions(statisticsService);
    }
}
