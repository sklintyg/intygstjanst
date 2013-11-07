package se.inera.certificate.integration;

import javax.persistence.PersistenceException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;
import se.inera.certificate.integration.util.NamespacePrefixNameIgnoringListener;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterMedicalCertificateResponderWiretapImplTest {

    @Mock
    CertificateService certificateService = mock(CertificateService.class);

    @InjectMocks
    private RegisterMedicalCertificateResponderWiretapImpl responder = new RegisterMedicalCertificateResponderWiretapImpl();

    private RegisterMedicalCertificateType request;
    private String xml;

    @Before
    public void initializeResponder() throws JAXBException {
        responder.initializeJaxbContext();
    }

    @Before
    public void prepareRequest() throws Exception {

        ClassPathResource file = new ClassPathResource(
                "RegisterMedicalCertificateLegacyResponderProviderTest/fk7263.xml");
        JAXBContext context = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
        JAXBElement<RegisterMedicalCertificateType> e = context.createUnmarshaller().unmarshal(
                new StreamSource(file.getInputStream()), RegisterMedicalCertificateType.class);
        request = e.getValue();

        xml = FileUtils.readFileToString(file.getFile());
    }

    @Test
    public void testIt() throws Exception {
        Certificate certificate = new Certificate("123", "<utlatande/>");

        ArgumentCaptor<String> xmlCaptor = ArgumentCaptor.forClass(String.class);
        when(certificateService.storeCertificate(xmlCaptor.capture(), eq("fk7263"))).thenReturn(certificate);

        RegisterMedicalCertificateResponseType response = responder.registerMedicalCertificate(null, request);

        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());

        compareSoapMessageWithReferenceFile(xmlCaptor.getValue());

        verify(certificateService).setCertificateState(eq("19121212-1212"), eq("6ea04fd0-5fef-4809-823b-efeddf8a4d55"),
                eq("FK"), eq(CertificateState.SENT), any(LocalDateTime.class));
    }

    @Test
    public void testWithServiceThrowingPersistenceException() throws Exception {
        when(certificateService.storeCertificate(any(String.class), eq("fk7263")))
                .thenThrow(new PersistenceException());

        RegisterMedicalCertificateResponseType response = responder.registerMedicalCertificate(null, request);
        assertEquals(ResultCodeEnum.INFO, response.getResult().getResultCode());
    }

    private void compareSoapMessageWithReferenceFile(String xmlCaptorValue) throws IOException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);
        Diff diff = new Diff(xmlCaptorValue, xml);
        diff.overrideDifferenceListener(new NamespacePrefixNameIgnoringListener());
        assertTrue(diff.toString(), diff.identical());
    }
}
