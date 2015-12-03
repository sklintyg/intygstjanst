package se.inera.intyg.intygstjanst.web.integration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.ifv.insuranceprocess.certificate.v1.StatusType;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatus.rivtabp20.v1.SetCertificateStatusResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusRequestType;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;


/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class SetCertificateStatusResponderImplTest {

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private SetCertificateStatusResponderInterface responder = new SetCertificateStatusResponderImpl();

    @Test
    public void testSetCertificateStatus() throws Exception {

        LocalDateTime timestamp = new LocalDateTime(2013, 4, 26, 12, 0, 0);

        SetCertificateStatusRequestType request = new SetCertificateStatusRequestType();
        request.setCertificateId("no5");
        request.setNationalIdentityNumber("19001122-3344");
        request.setStatus(StatusType.CANCELLED);
        request.setTarget("försäkringskassan");
        request.setTimestamp(timestamp);

        responder.setCertificateStatus(null, request);

        verify(certificateService).setCertificateState(new Personnummer("19001122-3344"), "no5", "försäkringskassan", CertificateState.CANCELLED, timestamp);
    }
}
