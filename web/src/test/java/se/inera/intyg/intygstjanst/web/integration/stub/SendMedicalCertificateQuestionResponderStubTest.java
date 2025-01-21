/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.web.integration.stub;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.Amnetyp;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.InnehallType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.intyg.common.support.stub.MedicalCertificatesStore;

@RunWith(MockitoJUnitRunner.class)
public class SendMedicalCertificateQuestionResponderStubTest {

    @Mock
    MedicalCertificatesStore store;

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
        question.setLakarutlatande(lakarutlatande);
        request.setQuestion(question);

        stub.sendMedicalCertificateQuestion(logicalAddress, request);

        verify(store).makulera("id-1234567890", "meddelande");
    }
}
