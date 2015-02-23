package se.inera.certificate.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType.OK;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.certificate.clinicalprocess.healthcond.certificate.sendcertificateforcitizen.v1.SendCertificateForCitizenResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.sendcertificateforcitizen.v1.SendCertificateForCitizenResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.sendcertificateforcitizen.v1.SendCertificateForCitizenType;
import se.inera.certificate.model.builder.CertificateBuilder;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateDao;
import se.inera.certificate.service.CertificateSenderService;
import se.inera.certificate.service.CertificateService;

@RunWith( MockitoJUnitRunner.class )
public class SendCertificateForCitizenResponderImplTest {

    private static final String PERSONNUMMER = "19121212-1212";
    private static final String CERTIFICATE_ID = "Intygs-id-1234567890";
    private static final String RECIPIENT_ID = "FK";

    private static final String LOGICAL_ADDRESS = "Intygstjänsten";

    @Mock
    private CertificateDao certificateDao;

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @Mock
    private CertificateSenderService certificateSenderService;

    @InjectMocks
    private SendCertificateForCitizenResponderInterface responder = new SendCertificateForCitizenResponderImpl();

    @Test
    public void testSendOk() throws Exception {
        Certificate certificate = new CertificateBuilder(CERTIFICATE_ID).build();

        // Setup mocks
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID)).thenReturn(CertificateService.SendStatus.OK);

        // Make the call
        SendCertificateForCitizenResponseType response = responder.sendCertificateForCitizen(LOGICAL_ADDRESS, createRequest());

        // Verify behaviour
        assertEquals(OK, response.getResult().getResultCode());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID);
    }

    private SendCertificateForCitizenType createRequest() {

        SendCertificateForCitizenType request = new SendCertificateForCitizenType();
        request.setPersonId(PERSONNUMMER);
        request.setMottagareId(RECIPIENT_ID);
        request.setUtlatandeId(CERTIFICATE_ID);

        return request;
    }
}
