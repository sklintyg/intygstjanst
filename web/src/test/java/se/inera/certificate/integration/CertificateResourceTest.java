package se.inera.certificate.integration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Matchers.anyObject;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import se.inera.certificate.integration.test.CertificateResource;
import se.inera.certificate.model.dao.Certificate;

@RunWith(MockitoJUnitRunner.class)
public class CertificateResourceTest {

    @Mock
    private EntityManager entityManager = mock(EntityManager.class);

    @Mock
    private PlatformTransactionManager txManager = mock(PlatformTransactionManager.class);

    @Mock
    private TransactionStatus txStatus = mock(TransactionStatus.class);

    @InjectMocks
    private CertificateResource certificateResource = new CertificateResource() {
        @Override protected String marshall(Certificate certificate) { return ""; }
    };

    @Before
    public void injectTxManager() {
        certificateResource.setTxManager(txManager);
    }

    @Test
    public void testGetCertificate() throws Exception {
        certificateResource.getCertificate("1");

        verify(entityManager).find(Certificate.class, "1");
    }

    @Test
    public void testDeleteCertificate() throws Exception {
        Certificate certificate = new Certificate("1", "");
        when(txManager.getTransaction((TransactionDefinition) anyObject())).thenReturn(txStatus);
        when(entityManager.find(Certificate.class, "1")).thenReturn(certificate);

        certificateResource.deleteCertificate("1");

        verify(entityManager).find(Certificate.class, "1");
        verify(entityManager).remove(certificate);
        verify(txManager).commit(txStatus);
    }

    @Test
    public void testDeleteCertificateHandlesException() throws Exception {
        Certificate certificate = new Certificate("1", "");
        when(txManager.getTransaction((TransactionDefinition) anyObject())).thenReturn(txStatus);
        when(entityManager.find(Certificate.class, "1")).thenReturn(certificate);
        
        doThrow(new RuntimeException("")).when(entityManager).remove(certificate);

        certificateResource.deleteCertificate("1");

        verify(txStatus).setRollbackOnly();
    }

    @Test
    public void testInsertCertificate() throws Exception {
        Certificate certificate = new Certificate("1", "");
        when(txManager.getTransaction((TransactionDefinition) anyObject())).thenReturn(txStatus);
        certificateResource.insertCertificate(certificate);

        verify(entityManager).persist(certificate);
        verify(txManager).commit(txStatus);
    }

    @Test
    public void testInsertCertificateHandlesException() throws Exception {
        Certificate certificate = new Certificate("1", "");
        when(txManager.getTransaction((TransactionDefinition) anyObject())).thenReturn(txStatus);
        doThrow(new RuntimeException("")).when(entityManager).persist(certificate);

        certificateResource.insertCertificate(certificate);

        verify(txStatus).setRollbackOnly();
    }
}
