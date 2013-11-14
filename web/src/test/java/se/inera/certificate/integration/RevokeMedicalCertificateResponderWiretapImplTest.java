package se.inera.certificate.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.joda.time.LocalDateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.exception.CertificateRevokedException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateStateHistoryEntry;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
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
