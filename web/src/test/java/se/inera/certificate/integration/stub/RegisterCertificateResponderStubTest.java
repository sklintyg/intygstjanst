package se.inera.certificate.integration.stub;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.utils.IdUtil;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.ModuleApi;

@RunWith(MockitoJUnitRunner.class)
public class RegisterCertificateResponderStubTest {

    private static final String UTLATANDE_ID = "id-1234567890";

    @Mock
    FkMedicalCertificatesStore store;

    @Mock
    IntygModuleRegistry moduleRegistry;

    @Mock
    ModuleApi moduleApi;

    @InjectMocks
    RegisterCertificateResponderStub stub = new RegisterCertificateResponderStub() {
        @Override
        protected void validate(RegisterCertificateType registerCertificate, ModuleApi module) {
        }
    };

    @SuppressWarnings("unchecked")
    @Test
    public void testName() throws Exception {
        String logicalAddress = "ts-bas";
        // read request from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RegisterCertificateType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        RegisterCertificateType request = unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("fk7263/utlatande.xml").getInputStream()), RegisterCertificateType.class).getValue();

        when(moduleRegistry.getModuleApi(any(String.class))).thenReturn(moduleApi);

        request.getUtlatande().setUtlatandeId(IdUtil.generateId(UTLATANDE_ID));

        stub.registerCertificate(logicalAddress, request);

        verify(store).addCertificate(eq(UTLATANDE_ID), any(Map.class));
    }
}
