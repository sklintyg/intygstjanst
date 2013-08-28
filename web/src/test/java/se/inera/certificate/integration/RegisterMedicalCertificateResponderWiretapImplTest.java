package se.inera.certificate.integration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Holder;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.OK;

import intyg.registreraintyg._1.RegistreraIntygResponderInterface;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.certificate.common.v1.Utlatande;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

/**
 * @author andreaskaltenbach
 */
@RunWith( MockitoJUnitRunner.class )
public class RegisterMedicalCertificateResponderWiretapImplTest {

    @Mock
    private RegistreraIntygResponderInterface registreraIntygResponder = mock(RegistreraIntygResponderInterface.class);

    @Mock
    private CertificateService certificateService;

    @InjectMocks
    private RegisterMedicalCertificateResponderInterface responder = new RegisterMedicalCertificateResponderWiretapImpl();

    @Test
    public void testRegisterCertificate() throws JAXBException, IOException {

        // read request from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<RegisterMedicalCertificateType> request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("fk7263/fk7263.xml").getInputStream()), RegisterMedicalCertificateType.class);

        RegisterMedicalCertificateResponseType response = responder.registerMedicalCertificate(null, request.getValue());

        verify(registreraIntygResponder).registreraIntyg(Matchers.<Holder<Utlatande>>any());
        verify(certificateService).storeOriginalCertificate(request.getValue());

        String personnummer = request.getValue().getLakarutlatande().getPatient().getPersonId().getExtension();
        String certificateId = request.getValue().getLakarutlatande().getLakarutlatandeId();

        verify(certificateService).setCertificateState(eq(personnummer), eq(certificateId), eq("FK"), eq(CertificateState.SENT), any(LocalDateTime.class));

        verifyNoMoreInteractions(registreraIntygResponder, certificateService);

        assertEquals(OK, response.getResult().getResultCode());
    }
}
