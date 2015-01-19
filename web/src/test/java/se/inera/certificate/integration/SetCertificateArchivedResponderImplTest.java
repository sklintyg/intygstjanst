package se.inera.certificate.integration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatearchivedresponder.v1.SetCertificateArchivedRequestType;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class SetCertificateArchivedResponderImplTest {

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @InjectMocks
    private SetCertificateArchivedResponderImpl responder = new SetCertificateArchivedResponderImpl();

    @Test
    public void testSetCertificateArchived() throws Exception {

        SetCertificateArchivedRequestType request = new SetCertificateArchivedRequestType();
        request.setCertificateId("no5");
        request.setNationalIdentityNumber("19001122-3344");
        request.setArchivedState("true");
        responder.setCertificateArchived(null, request);

        verify(certificateService).setArchived("no5", "19001122-3344", "true");
    }
}
