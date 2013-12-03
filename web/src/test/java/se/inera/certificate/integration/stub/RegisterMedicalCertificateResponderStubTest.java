package se.inera.certificate.integration.stub;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

@RunWith(MockitoJUnitRunner.class)
public class RegisterMedicalCertificateResponderStubTest {
    
    @Mock
    FkMedicalCertificatesStore store;

    @InjectMocks
    RegisterMedicalCertificateResponderStub stub = new RegisterMedicalCertificateResponderStub() {
        @Override
        protected void validate(RegisterMedicalCertificateType registerMedicalCertificate) {}
    };

    @SuppressWarnings("unchecked")
    @Test
    public void testName() throws JAXBException, IOException {
        AttributedURIType logicalAddress = new AttributedURIType();
        // read request from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        RegisterMedicalCertificateType request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("fk7263/fk7263.xml").getInputStream()), RegisterMedicalCertificateType.class).getValue();

        request.getLakarutlatande().setLakarutlatandeId("id-1234567890");
        
        stub.registerMedicalCertificate(logicalAddress, request);
        
        verify(store).addCertificate(eq("id-1234567890"), any(Map.class));
    }
}
