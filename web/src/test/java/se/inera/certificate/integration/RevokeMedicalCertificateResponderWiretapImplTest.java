package se.inera.certificate.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.model.dao.Certificate;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;

@RunWith(MockitoJUnitRunner.class)
public class RevokeMedicalCertificateResponderWiretapImplTest extends RevokeMedicalCertificateResponderImplTest {

    @Override
    protected RevokeMedicalCertificateResponderInterface createResponder() {
        return new RevokeMedicalCertificateResponderWiretapImpl();
    };

    @Test
    @Override
    public void testRevokeCertificateWhichWasAlreadySentToForsakringskassan() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID, "text");

        when(certificateService.revokeCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        verify(certificateService).revokeCertificate(PERSONNUMMER, CERTIFICATE_ID);

        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
    }
    
    @Override
    public void testRevokeCertificateWithForsakringskassanReturningError() {
        // This is not a valid case for wiretap (no communication with Forsakringskassan so no error can occur).
    }

}
