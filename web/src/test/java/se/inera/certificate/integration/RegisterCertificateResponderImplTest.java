package se.inera.certificate.integration;

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
import static org.mockito.Mockito.when;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.exception.CertificateAlreadyExistsException;
import se.inera.certificate.integration.util.NamespacePrefixNameIgnoringListener;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.StatisticsService;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterCertificateResponderImplTest {

    private RegisterCertificateType request;
    private String xml;

    @Mock
    CertificateService certificateService = mock(CertificateService.class);

    @Mock
    StatisticsService statisticsService = mock(StatisticsService.class);

    @InjectMocks
    private RegisterCertificateResponderImpl responder = new RegisterCertificateResponderImpl();

    @Before
    public void initializeResponder() throws JAXBException {
        responder.initializeJaxbContext();
    }

    @Before
    public void prepareRequest() throws Exception {

        ClassPathResource file = new ClassPathResource("RegisterCertificateResponderImplTest/utlatande.xml");

        xml = FileUtils.readFileToString(file.getFile());

        JAXBContext context = JAXBContext.newInstance(UtlatandeType.class);
        JAXBElement<UtlatandeType> e = context.createUnmarshaller().unmarshal(new StreamSource(file.getInputStream()),
                UtlatandeType.class);
        request = new RegisterCertificateType();
        request.setUtlatande(e.getValue());
    }

    @Test
    public void test() throws Exception {
        Certificate certificate = new Certificate("123", "<utlatande/>");

        ArgumentCaptor<String> xmlCaptor = ArgumentCaptor.forClass(String.class);
        when(certificateService.storeCertificate(xmlCaptor.capture(), eq("fk7263"))).thenReturn(certificate);

        RegisterCertificateResponseType response = responder.registerCertificate(null, request);

        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
        compareSoapMessageWithReferenceFile(xmlCaptor.getValue());
        Mockito.verify(statisticsService, Mockito.only()).created(certificate);
    }

    @Test
    public void testWithExistingCertificate() throws Exception {
        when(certificateService.storeCertificate(any(String.class), eq("fk7263")))
                .thenThrow(new CertificateAlreadyExistsException(request.getUtlatande().getUtlatandeId().toString()));

        RegisterCertificateResponseType response = responder.registerCertificate(null, request);
        assertEquals(ResultCodeType.INFO, response.getResult().getResultCode());
        Mockito.verifyZeroInteractions(statisticsService);
    }

    private void compareSoapMessageWithReferenceFile(String xmlCaptorValue) throws IOException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);
        Diff diff = new Diff(xmlCaptorValue, xml);
        diff.overrideDifferenceListener(new NamespacePrefixNameIgnoringListener());
        assertTrue(diff.toString(), diff.identical());
    }

}