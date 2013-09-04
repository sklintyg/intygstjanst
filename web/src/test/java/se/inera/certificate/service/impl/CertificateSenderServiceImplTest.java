package se.inera.certificate.service.impl;

import javax.ws.rs.core.Response;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.builder.CertificateBuilder;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateSenderServiceImplTest {

    @Mock
    private CertificateService certificateService;

    @Mock
    private ModuleRestApiFactory moduleRestApiFactory;

    @Mock
    private ModuleRestApi moduleRestApi;

    @Mock
    private DispatchFactory dispatchFactory;
    @Mock
    private Dispatch<SOAPMessage> dispatch;

    @Mock
    private Response response = mock(Response.class);

    private static String certificateXml;

    private static Certificate certificate = new CertificateBuilder("123456").certificateType("fk7263").build();
    private static Utlatande utlatande;

    private static SOAPMessage soapErrorMessage;
    private static SOAPMessage soapOkMessage;

    @BeforeClass
    public static void readCertificateXml() throws IOException {
        certificateXml = FileUtils.readFileToString(new ClassPathResource(
                "CertificateSenderServiceImplTest/utlatande.xml").getFile());
    }

    @BeforeClass
    public static void setupSoapMessages() throws Exception {
        soapErrorMessage = soapMessageFromFile("CertificateSenderServiceImplTest/soap-message-register-error.xml");
        soapOkMessage = soapMessageFromFile("CertificateSenderServiceImplTest/soap-message-register-ok.xml");
    }

    @BeforeClass
    public static void setupUtlatande() throws Exception {
        utlatande = new CustomObjectMapper().readValue(
                new ClassPathResource("lakarutlatande/maximalt-fk7263.json").getFile(), Utlatande.class);
    }

    @Before
    public void setLogicalAddress() {
        senderService.logicalAddress = "someLogicalAddress";
    }

    @Before
    public void setupModuleRestApiFactory() {
        when(moduleRestApiFactory.getModuleRestService(any(Utlatande.class))).thenReturn(moduleRestApi);
    }

    @Before
    public void setupDispatchFactory() {
        when(dispatchFactory.dispatchForRegisterMedicalCertificate()).thenReturn(dispatch);
    }

    @Before
    public void setupCertificateService() {
        when(certificateService.getLakarutlatande(certificate)).thenReturn(utlatande);
    }

    @InjectMocks
    private CertificateSenderServiceImpl senderService = new CertificateSenderServiceImpl();

    @Test
    public void testSend() throws Exception {

        // Module API mock returns transport XML for certificate (OK)
        okResponse();
        when(moduleRestApi.marshall(eq("1.0"), anyString())).thenReturn(response);

        // setup captor for verifying outbound SOAP message
        ArgumentCaptor<SOAPMessage> soapMessage = ArgumentCaptor.forClass(SOAPMessage.class);
        when(dispatch.invoke(soapMessage.capture())).thenReturn(soapOkMessage);

        senderService.sendCertificate(certificate, "fk");

        compareSoapMessageWithReferenceFile(soapMessage.getValue(), "CertificateSenderServiceImplTest/soap-message.xml");
    }

    private void okResponse() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.hasEntity()).thenReturn(true);
        when(response.getEntity()).thenReturn(
                new ClassPathResource("CertificateSenderServiceImplTest/utlatande.xml").getInputStream());
    }

    private void errorResponse() throws Exception {
        when(response.getStatus()).thenReturn(500);
    }

    private void compareSoapMessageWithReferenceFile(SOAPMessage soapMessage, String fileName) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        soapMessage.writeTo(outputStream);
        String registerMedicalCertificateRequest = outputStream.toString("UTF-8");

        String soapMessageXml = FileUtils.readFileToString(new ClassPathResource(fileName).getFile());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);
        Diff diff = new Diff(soapMessageXml, registerMedicalCertificateRequest);
        assertTrue(diff.toString(), diff.identical());
    }

    private static SOAPMessage soapMessageFromFile(String file) throws Exception {
        SOAPMessage message = MessageFactory.newInstance().createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        soapPart.setContent(new StreamSource(new ClassPathResource(file).getInputStream()));
        message.saveChanges();
        return message;
    }

    @Test(expected = WebServiceException.class)
    public void testSendWithReceiverDown() throws Exception {

        // Module API mock returns transport XML for certificate (OK)
        okResponse();
        when(moduleRestApi.marshall(eq("1.0"), anyString())).thenReturn(response);

        // web service call fails
        when(dispatch.invoke(any(SOAPMessage.class))).thenThrow(WebServiceException.class);

        senderService.sendCertificate(certificate, "fk");
    }

    @Test(expected = ExternalWebServiceCallFailedException.class)
    public void testSendWithUnsuccessfulCallToReceiver() throws Exception {

        // Module API mock returns transport XML for certificate (OK)
        okResponse();
        when(moduleRestApi.marshall(eq("1.0"), anyString())).thenReturn(response);

        // web service call returns resultOfCall = error
        when(dispatch.invoke(any(SOAPMessage.class))).thenReturn(soapErrorMessage);

        senderService.sendCertificate(certificate, "fk");

    }

    @Test(expected = RuntimeException.class)
    public void testSendWithFailingModule() throws Exception {
        // Module API mock returns with error (Internal Server Error)
        errorResponse();
        when(moduleRestApi.marshall(eq("1.0"), anyString())).thenReturn(response);

        senderService.sendCertificate(certificate, "fk");
    }

}
