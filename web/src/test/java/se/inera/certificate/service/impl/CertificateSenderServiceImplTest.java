package se.inera.certificate.service.impl;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import se.inera.certificate.service.recipientservice.RecipientBuilder;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateSenderServiceImplTest {

    private static final String CERTIFICATE_ID = "123456";
    private static final String CERTIFICATE_DOCUMENT = "{}";
    private static final String CERTIFICATE_TYPE = "fk7263";

    private static final String RECIPIENT_ID = "FK";
    private static final String RECIPIENT_NAME = "Försäkringskassan";
    private static final String RECIPIENT_LOGICALADDRESS = "FKORG";
    private static final String RECIPIENT_CERTIFICATETYPES = "fk7263";

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

    @InjectMocks
    private CertificateSenderServiceImpl senderService = new CertificateSenderServiceImpl();

    private static Certificate certificate = new CertificateBuilder(CERTIFICATE_ID, CERTIFICATE_DOCUMENT).certificateType(CERTIFICATE_TYPE).build();

    private static RegisterMedicalCertificateResponseType errorWsMessage;
    private static RegisterMedicalCertificateResponseType okWsMessage;

    private static Recipient createRecipient() {
        RecipientBuilder builder = new RecipientBuilder();
        builder.setId(RECIPIENT_ID);
        builder.setName(RECIPIENT_NAME);
        builder.setLogicalAddress(RECIPIENT_LOGICALADDRESS);
        builder.setCertificateTypes(RECIPIENT_CERTIFICATETYPES);

        return builder.build();
    }

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
        when(moduleEntryPoint.getDefaultRecipient()).thenReturn(RECIPIENT_LOGICALADDRESS);
    }

    @Before
    public void setupRecipientService() throws RecipientUnknownException {
        when(recipientService.getVersion(RECIPIENT_LOGICALADDRESS, CERTIFICATE_TYPE)).thenReturn(TransportModelVersion.LEGACY_LAKARUTLATANDE);
        when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(createRecipient());
        when(recipientService.getRecipientForLogicalAddress(RECIPIENT_LOGICALADDRESS)).thenReturn(createRecipient());
    }

    @Test
    public void testSend() throws Exception {
        senderService.sendCertificate(certificate, RECIPIENT_ID);
        verify(moduleApi).sendCertificateToRecipient(Mockito.any(InternalModelHolder.class), Mockito.eq(RECIPIENT_LOGICALADDRESS));
    }

    @Test(expected = ServerException.class)
    public void testSendWithFailingModule() throws Exception {
        // web service call fails
        Mockito.doThrow(new ModuleException("")).when(moduleApi).sendCertificateToRecipient(Mockito.any(InternalModelHolder.class), Mockito.eq(RECIPIENT_LOGICALADDRESS));
        senderService.sendCertificate(certificate, RECIPIENT_ID);
    }

}
