package se.inera.certificate.service.impl;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.certificate.exception.RecipientUnknownException;
import se.inera.certificate.exception.ServerException;
import se.inera.certificate.model.builder.CertificateBuilder;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.TransportModelVersion;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.RecipientService;
import se.inera.certificate.service.recipientservice.Recipient;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateSenderServiceImplTest {

    private static final String LOGICAL_ADDRESS = "FK";

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

    private static Certificate certificate = new CertificateBuilder("123456", "{}").certificateType("fk7263").build();

    private static RegisterMedicalCertificateResponseType errorWsMessage;
    private static RegisterMedicalCertificateResponseType okWsMessage;

    public RegisterMedicalCertificateType request() throws IOException, JAXBException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(RegisterMedicalCertificateResponseType.class).createUnmarshaller();
        return unmarshaller.unmarshal(new StreamSource(new ClassPathResource("CertificateSenderServiceImplTest/utlatande.xml").getInputStream()),
                RegisterMedicalCertificateType.class).getValue();
    }

    @BeforeClass
    public static void setupSoapMessages() throws Exception {

        Unmarshaller unmarshaller = JAXBContext.newInstance(RegisterMedicalCertificateResponseType.class).createUnmarshaller();
        okWsMessage = unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("CertificateSenderServiceImplTest/soap-message-register-ok.xml").getInputStream()),
                RegisterMedicalCertificateResponseType.class).getValue();
        errorWsMessage = unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("CertificateSenderServiceImplTest/soap-message-register-error.xml").getInputStream()),
                RegisterMedicalCertificateResponseType.class).getValue();
    }

    @Before
    public void setupModuleRestApiFactory() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleEntryPoint(anyString())).thenReturn(moduleEntryPoint);
        when(moduleEntryPoint.getModuleApi()).thenReturn(moduleApi);
        when(moduleEntryPoint.getModuleId()).thenReturn("fk7263");
        when(moduleEntryPoint.getDefaultRecieverLogicalAddress()).thenReturn(LOGICAL_ADDRESS);
    }

    @Before
    public void setupRecipientService() throws RecipientUnknownException {
        when(recipientService.getVersion("FK", "fk7263")).thenReturn(TransportModelVersion.LEGACY_LAKARUTLATANDE);
        when(recipientService.getRecipient("FK")).thenReturn(new Recipient("FK", "Försäkringskassan", "fk"));
    }

    @InjectMocks
    private CertificateSenderServiceImpl senderService = new CertificateSenderServiceImpl();

    @Test
    public void testSend() throws Exception {

        senderService.sendCertificate(certificate, "FK");

        verify(moduleApi).sendCertificateToRecipient(Mockito.any(InternalModelHolder.class), Mockito.eq(LOGICAL_ADDRESS));
    }

    @Test(expected = ServerException.class)
    public void testSendWithFailingModule() throws Exception {

        // web service call fails
        Mockito.doThrow(new ModuleException("")).when(moduleApi).sendCertificateToRecipient(Mockito.any(InternalModelHolder.class), Mockito.eq(LOGICAL_ADDRESS));

        senderService.sendCertificate(certificate, "FK");
    }

}
