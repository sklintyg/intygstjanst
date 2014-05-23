package se.inera.certificate.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.OK;
import iso.v21090.dt.v1.II;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;

import riv.insuranceprocess.healthreporting.medcertqa._1.LakarutlatandeEnkelType;
import riv.insuranceprocess.healthreporting.medcertqa._1.VardAdresseringsType;
import se.inera.certificate.exception.ClientException;
import se.inera.certificate.model.builder.CertificateBuilder;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType;

@RunWith( MockitoJUnitRunner.class )
public class SendMedicalCertificateResponderImplTest {

    private static final String CERTIFICATE_ID = "Intygs-id-1234567890";
    private static final String PERSONNUMMER = "19121212-1212";
    private static final String PATIENT_ID_OID = "1.2.752.129.2.1.3.1";
    private static final String HOS_PERSONAL_OID = "1.2.752.129.2.1.4.1";
    private static final String ENHET_OID = "1.2.752.129.2.1.4.1";
    private static final String ARBETSPLATS_CODE_OID = "1.2.752.29.4.71";


    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @InjectMocks
    private SendMedicalCertificateResponderInterface responder = new SendMedicalCertificateResponderImpl();

    @Test
    public void testSend() throws ClientException {
        when(certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(new CertificateBuilder(CERTIFICATE_ID).build());
        AttributedURIType uri = new AttributedURIType();
        uri.setValue("FK");
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(uri, createRequest());

        assertEquals(OK, response.getResult().getResultCode());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "FK");
    }

    private SendMedicalCertificateRequestType createRequest() {
        SendMedicalCertificateRequestType request = new SendMedicalCertificateRequestType();
        SendType sendType = new SendType();
        
        VardAdresseringsType vardAdresseringsType = new VardAdresseringsType();
        HosPersonalType hosPersonal = new HosPersonalType();

        // Enhet
        EnhetType enhet = new EnhetType();
        enhet.setEnhetsnamn("enhetsnamn");
        II enhetsId = new II();
        enhetsId.setRoot(ENHET_OID);
        enhetsId.setExtension("enhetsid");
        enhet.setEnhetsId(enhetsId);
        II arbetsplatsKod = new II();
        arbetsplatsKod.setRoot(ARBETSPLATS_CODE_OID);
        arbetsplatsKod.setExtension("arbetsplatskod");
        enhet.setArbetsplatskod(arbetsplatsKod);
        VardgivareType vardGivare = new VardgivareType();
        II vardGivarId = new II();
        vardGivarId.setRoot(ENHET_OID);
        vardGivarId.setExtension("vardgivarid");
        vardGivare.setVardgivareId(vardGivarId);
        vardGivare.setVardgivarnamn("MI");
        enhet.setVardgivare(vardGivare);
        hosPersonal.setEnhet(enhet);
        hosPersonal.setFullstandigtNamn("MI");
        II personalId = new II();
        personalId.setRoot(HOS_PERSONAL_OID);
        personalId.setExtension("MI");
        hosPersonal.setPersonalId(personalId);
        hosPersonal.setFullstandigtNamn("hospersonal namn");
        vardAdresseringsType.setHosPersonal(hosPersonal);

        sendType.setAdressVard(vardAdresseringsType);
        sendType.setAvsantTidpunkt(new LocalDateTime());
        sendType.setVardReferensId("MI");

        // Lakarutlatande
        LakarutlatandeEnkelType lakarutlatande = new LakarutlatandeEnkelType();
        lakarutlatande.setLakarutlatandeId(CERTIFICATE_ID);
        lakarutlatande.setSigneringsTidpunkt(new LocalDateTime());
        PatientType patient = new PatientType();
        II patientIdHolder = new II();
        patientIdHolder.setRoot(PATIENT_ID_OID);
        patientIdHolder.setExtension(PERSONNUMMER);
        patient.setPersonId(patientIdHolder);
        patient.setFullstandigtNamn("patientnamn");
        lakarutlatande.setPatient(patient);

        sendType.setLakarutlatande(lakarutlatande);        

        request.setSend(sendType);

        return request;
    }
}
