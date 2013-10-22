package se.inera.certificate.integration;

import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.validate.ValidationException;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterMedicalCertificateResponderImplTest {
    @Mock
    ModuleRestApiFactory moduleRestApiFactory = mock(ModuleRestApiFactory.class);

    @Mock
    CertificateService certificateService = mock(CertificateService.class);

    @Mock
    ModuleRestApi moduleRestApi = mock(ModuleRestApi.class);

    @Mock
    Response response = mock(Response.class);

    @InjectMocks
    private RegisterMedicalCertificateResponderImpl responder = new RegisterMedicalCertificateResponderImpl();

    @Test
    public void testHappyFlow() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage soapMessage   = mf.createMessage();

        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        df.setNamespaceAware(true);
        DocumentBuilder docBuilder = df.newDocumentBuilder();
        Document doc = docBuilder.parse(new ClassPathResource("register-medical-certificate/register-medical-certificate.xml").getInputStream());;
        soapMessage.getSOAPBody().addDocument(doc);

        ArgumentCaptor<String> xmlText = ArgumentCaptor.forClass(String.class);

        when(moduleRestApiFactory.getModuleRestService("fk7263")).thenReturn(moduleRestApi);
        when(moduleRestApi.unmarshall(xmlText.capture())).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.hasEntity()).thenReturn(true);
        when(response.getEntity()).thenReturn(new ClassPathResource("lakarutlatande/maximalt-fk7263.json").getInputStream());

        ArgumentCaptor<Utlatande> utlatande = ArgumentCaptor.forClass(Utlatande.class);
        ArgumentCaptor<String> jsonText = ArgumentCaptor.forClass(String.class);

        responder.invoke(soapMessage);

        verify(certificateService).storeCertificate(utlatande.capture(), jsonText.capture());

        Utlatande u = utlatande.getValue();

        assertEquals("80832895-5a9c-450a-bd74-08af43750788", u.getId().getExtension());
        assertNotNull(jsonText.getValue().length());
        assertNotNull(xmlText.getValue().length());
    }

    @Test(expected = ValidationException.class)
    public void testModuleNotFound() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage soapMessage   = mf.createMessage();

        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        df.setNamespaceAware(true);
        DocumentBuilder docBuilder = df.newDocumentBuilder();
        Document doc = docBuilder.parse(new ClassPathResource("register-medical-certificate/register-medical-certificate.xml").getInputStream());;
        soapMessage.getSOAPBody().addDocument(doc);

        ArgumentCaptor<String> xmlText = ArgumentCaptor.forClass(String.class);

        when(moduleRestApiFactory.getModuleRestService("fk7263")).thenReturn(moduleRestApi);
        when(moduleRestApi.unmarshall(xmlText.capture())).thenReturn(response);
        when(response.getStatus()).thenReturn(404);
        when(response.hasEntity()).thenReturn(false);

        responder.invoke(soapMessage);

        verify(certificateService).storeCertificate(isA(Utlatande.class), isA(String.class));
    }

    @Test
    public void testModuleError() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage soapMessage   = mf.createMessage();

        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        df.setNamespaceAware(true);
        DocumentBuilder docBuilder = df.newDocumentBuilder();
        Document doc = docBuilder.parse(new ClassPathResource("register-medical-certificate/register-medical-certificate.xml").getInputStream());;
        soapMessage.getSOAPBody().addDocument(doc);

        ArgumentCaptor<String> xmlText = ArgumentCaptor.forClass(String.class);

        when(moduleRestApiFactory.getModuleRestService("fk7263")).thenReturn(moduleRestApi);
        when(moduleRestApi.unmarshall(xmlText.capture())).thenReturn(response);
        when(response.getStatus()).thenReturn(400);
        when(response.hasEntity()).thenReturn(true);
        when(response.getEntity()).thenReturn(IOUtils.toInputStream("Error! 123"));

        try {
            responder.invoke(soapMessage);
            fail("No exception thrown!");
        } catch (Exception e) {
            assertEquals("Error! 123",e.getMessage());
        }
    }

}
