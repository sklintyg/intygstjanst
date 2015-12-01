package se.inera.certificate.integration.stub;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.intyg.intygstyper.fk7263.integration.stub.FkMedicalCertificatesStore;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.Amnetyp;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.InnehallType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;

@RunWith(MockitoJUnitRunner.class)
public class SendMedicalCertificateQuestionResponderStubTest {

    @Mock
    FkMedicalCertificatesStore store;
    
    @InjectMocks
    SendMedicalCertificateQuestionResponderStub stub = new SendMedicalCertificateQuestionResponderStub();
    
    @Test
    public void test() throws Exception {
        AttributedURIType logicalAddress = new AttributedURIType();
        SendMedicalCertificateQuestionType request = new SendMedicalCertificateQuestionType();
        QuestionToFkType question = new QuestionToFkType();
        question.setAmne(Amnetyp.MAKULERING_AV_LAKARINTYG);
        InnehallType innehall = new InnehallType();
        innehall.setMeddelandeText("meddelande");
        question.setFraga(innehall);
        LakarutlatandeEnkelType lakarutlatande = new LakarutlatandeEnkelType();
        lakarutlatande.setLakarutlatandeId("id-1234567890");
        question.setLakarutlatande(lakarutlatande );
        request.setQuestion(question );
        
        stub.sendMedicalCertificateQuestion(logicalAddress, request);
        
        verify(store).makulera("id-1234567890", "meddelande");
    }
}
