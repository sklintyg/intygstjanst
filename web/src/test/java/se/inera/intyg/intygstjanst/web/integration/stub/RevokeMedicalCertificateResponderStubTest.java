package se.inera.intyg.intygstjanst.web.integration.stub;

import static org.mockito.Mockito.verify;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.intyg.intygstyper.fk7263.integration.stub.FkMedicalCertificatesStore;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;

@RunWith(MockitoJUnitRunner.class)
public class RevokeMedicalCertificateResponderStubTest {

    private static final String UTLATANDE_ID = "intygs-id-1234567890";

    private static final String REVOKE_MESSAGE = "Meddelande";

    @Mock
    FkMedicalCertificatesStore store;

    @InjectMocks
    RevokeMedicalCertificateResponderStub stub = new RevokeMedicalCertificateResponderStub();

    @Test
    public void testName() throws Exception {
        // read request from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RevokeMedicalCertificateRequestType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        RevokeMedicalCertificateRequestType request = unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("revoke-medical-certificate/revoke-medical-certificate-request.xml").getInputStream()),
                RevokeMedicalCertificateRequestType.class).getValue();

        stub.revokeMedicalCertificate(null, request);

        verify(store).makulera(UTLATANDE_ID, REVOKE_MESSAGE);
    }
}
