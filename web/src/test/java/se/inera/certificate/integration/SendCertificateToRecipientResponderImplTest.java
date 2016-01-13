package se.inera.certificate.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType.OK;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.model.builder.CertificateBuilder;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateDao;
import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.certificate.service.CertificateSenderService;
import se.inera.certificate.service.CertificateService;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;


@RunWith( MockitoJUnitRunner.class )
public class SendCertificateToRecipientResponderImplTest {

    private static final Personnummer PERSONNUMMER = new Personnummer("19121212-1212");
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
    private SendCertificateToRecipientResponderInterface responder = new SendCertificateToRecipientResponderImpl();

    @Test
    public void testSendOk() throws Exception {
        Certificate certificate = new CertificateBuilder(CERTIFICATE_ID).build();

        // Setup mocks
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID)).thenReturn(CertificateService.SendStatus.OK);

        // Make the call
        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        // Verify behaviour
        assertEquals(OK, response.getResult().getResultCode());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID);
    }

    private SendCertificateToRecipientType createRequest() {

        SendCertificateToRecipientType request = new SendCertificateToRecipientType();
        request.setPersonId(PERSONNUMMER.getPersonnummer());
        request.setMottagareId(RECIPIENT_ID);
        request.setUtlatandeId(CERTIFICATE_ID);

        return request;
    }
}
