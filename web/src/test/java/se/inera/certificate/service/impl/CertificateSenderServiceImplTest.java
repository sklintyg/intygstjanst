package se.inera.certificate.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.exception.RecipientUnknownException;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.model.builder.CertificateBuilder;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.TransportModelResponse;
import se.inera.certificate.modules.support.api.dto.TransportModelVersion;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.schema.util.ExternalWebServiceCallFailedException;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.RecipientService;
import se.inera.certificate.service.recipientservice.Recipient;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

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

    private static Certificate certificate = new CertificateBuilder("123456").certificateType("fk7263").build();

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

    private AttributedURIType logicalAddress() {
        AttributedURIType address = new AttributedURIType();
        address.setValue(LOGICAL_ADDRESS);
        return address;
    }

    @Test
    public void testSend() throws Exception {

        // Module API mock returns transport XML for certificate (OK)
        okResponse();

        // setup captor for verifying outbound SOAP message
        when(registerClient.registerMedicalCertificate(logicalAddress(), request())).thenReturn(okWsMessage);

        senderService.sendCertificate(certificate, "FK");

        verify(registerClient).registerMedicalCertificate(logicalAddress(), request());
    }

    private void okResponse() throws Exception {
    }

    private void errorResponse() throws Exception {
    }

    @Test(expected = ExternalWebServiceCallFailedException.class)
    public void testSendWithReceiverDown() throws Exception {

        // Module API mock returns transport XML for certificate (OK)
        okResponse();

        // web service call fails
        when(registerClient.registerMedicalCertificate(any(AttributedURIType.class), any(RegisterMedicalCertificateType.class))).thenReturn(
                errorWsMessage);

        senderService.sendCertificate(certificate, "FK");
    }

    @Test(expected = RuntimeException.class)
    public void testSendWithFailingModule() throws Exception {
        // Module API mock returns with error (Internal Server Error)
        errorResponse();

        senderService.sendCertificate(certificate, "FK");
    }

}
