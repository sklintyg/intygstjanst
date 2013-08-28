package se.inera.certificate.integration;

import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDateTime;
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
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.service.CertificateService;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterMedicalCertificateResponderWiretapImplTest {

    @Mock
    ModuleRestApiFactory moduleRestApiFactory = mock(ModuleRestApiFactory.class);

    @Mock
    CertificateService certificateService = mock(CertificateService.class);

    @Mock
    ModuleRestApi moduleRestApi = mock(ModuleRestApi.class);

    @Mock
    Response response = mock(Response.class);

    @InjectMocks
    private RegisterMedicalCertificateResponderWiretapImpl responder = new RegisterMedicalCertificateResponderWiretapImpl();

    @Test
    public void testIt() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage soapMessage = mf.createMessage();

        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        df.setNamespaceAware(true);
        DocumentBuilder docBuilder = df.newDocumentBuilder();
        Document doc = docBuilder.parse(new ClassPathResource("fk7263/fk7263.xml").getInputStream());

        soapMessage.getSOAPBody().addDocument(doc);

        ArgumentCaptor<String> xmlText = ArgumentCaptor.forClass(String.class);

        when(moduleRestApiFactory.getModuleRestService("fk7263")).thenReturn(moduleRestApi);
        when(moduleRestApi.unmarshall(xmlText.capture())).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.hasEntity()).thenReturn(true);
        when(response.getEntity()).thenReturn(
                new ClassPathResource("lakarutlatande/maximalt-fk7263.json").getInputStream());

        ArgumentCaptor<se.inera.certificate.model.Utlatande> utlatande = ArgumentCaptor
                .forClass(se.inera.certificate.model.Utlatande.class);
        ArgumentCaptor<String> jsonText = ArgumentCaptor.forClass(String.class);

        responder.invoke(soapMessage);

        verify(certificateService).storeCertificate(utlatande.capture(), jsonText.capture());

        se.inera.certificate.model.Utlatande u = utlatande.getValue();

        assertEquals("80832895-5a9c-450a-bd74-08af43750788", u.getId().getExtension());
        assertNotNull(jsonText.getValue().length());
        assertNotNull(xmlText.getValue().length());

        verify(certificateService).setCertificateState(eq("19121212-1212"), eq("6ea04fd0-5fef-4809-823b-efeddf8a4d55"), eq("FK"),
                eq(CertificateState.SENT), any(LocalDateTime.class));

        verifyNoMoreInteractions(certificateService);
    }
}
