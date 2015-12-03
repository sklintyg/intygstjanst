package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;


@RunWith(MockitoJUnitRunner.class)
public class RevokeMedicalCertificateResponderWiretapImplTest extends RevokeMedicalCertificateResponderImplTest {

    @Override
    protected RevokeMedicalCertificateResponderInterface createResponder() {
        return new RevokeMedicalCertificateResponderWiretapImpl();
    };

    @Test
    @Override
    public void testRevokeCertificateWhichWasAlreadySentToForsakringskassan() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID, "text");
        CertificateStateHistoryEntry historyEntry = new CertificateStateHistoryEntry(TARGET, CertificateState.SENT, new LocalDateTime());
        certificate.setStates(Collections.singletonList(historyEntry));

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        Mockito.verifyZeroInteractions(certificateSenderService);

        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
        Mockito.verify(statisticsService, Mockito.only()).revoked(certificate);
    }
    
    @Override
    public void testRevokeCertificateWithForsakringskassanReturningError() {
        // This is not a valid case for wiretap (no communication with Forsakringskassan so no error can occur).
    }

}
