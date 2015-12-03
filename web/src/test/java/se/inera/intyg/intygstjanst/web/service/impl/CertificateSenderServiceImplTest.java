package se.inera.intyg.intygstjanst.web.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.rivtabp20.v3.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.InternalModelHolder;
import se.inera.intyg.common.support.modules.support.api.dto.TransportModelVersion;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.builder.CertificateBuilder;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;

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
    private static final String RECIPIENT_DEFAULT_LOGICALADDRESS = "FKORG-DEFAULT";
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

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private CertificateSenderServiceImpl senderService = new CertificateSenderServiceImpl();

    private static Certificate certificate = new CertificateBuilder(CERTIFICATE_ID, CERTIFICATE_DOCUMENT).certificateType(CERTIFICATE_TYPE).originalCertificate("").build();

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
        when(moduleEntryPoint.getDefaultRecipient()).thenReturn(RECIPIENT_DEFAULT_LOGICALADDRESS);
    }

    @Before
    public void setupRecipientService() throws RecipientUnknownException {
        when(recipientService.getVersion(RECIPIENT_LOGICALADDRESS, CERTIFICATE_TYPE)).thenReturn(TransportModelVersion.LEGACY_LAKARUTLATANDE);
        when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(createRecipient());
        when(recipientService.getRecipientForLogicalAddress(RECIPIENT_LOGICALADDRESS)).thenReturn(createRecipient());
        when(recipientService.listRecipients(any(CertificateType.class))).thenReturn(Arrays.asList(createRecipient()));
    }

    @Test
    public void testSend() throws Exception {
        senderService.sendCertificate(certificate, RECIPIENT_ID);
        verify(moduleApi).sendCertificateToRecipient(any(InternalModelHolder.class), eq(RECIPIENT_LOGICALADDRESS), eq(RECIPIENT_ID));
    }
    
    @Test
    public void testSendWithDefaultRecipient() throws ModuleException {
        senderService.sendCertificate(certificate, null);
        verify(moduleApi).sendCertificateToRecipient(any(InternalModelHolder.class), eq(RECIPIENT_DEFAULT_LOGICALADDRESS), Mockito.isNull(String.class));
    }

    @Test(expected = ServerException.class)
    public void testSendWithFailingModule() throws Exception {
        // web service call fails
        doThrow(new ModuleException("")).when(moduleApi).sendCertificateToRecipient(any(InternalModelHolder.class), eq(RECIPIENT_LOGICALADDRESS), eq(RECIPIENT_ID));
        senderService.sendCertificate(certificate, RECIPIENT_ID);
    }

    @Test(expected = ServerException.class)
    public void testSendWithNoMatchingRecipient() {
        senderService.sendCertificate(certificate, "TS");
    }
}
