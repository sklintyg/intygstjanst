package se.inera.certificate.integration.test;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import se.inera.certificate.integration.converter.ConverterUtil;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;

@RunWith(MockitoJUnitRunner.class)
public class CertificateResourceTest {

    @Mock
    private EntityManager entityManager = mock(EntityManager.class);

    @Mock
    private PlatformTransactionManager txManager = mock(PlatformTransactionManager.class);

    @Mock
    private TransactionStatus txStatus = mock(TransactionStatus.class);

    @Mock
    private IntygModuleRegistry moduleRegistry = mock(IntygModuleRegistry.class);
    
    @Mock
    private ModuleApi moduleApi = mock(ModuleApi.class);

    @InjectMocks
    private CertificateResource certificateResource = new CertificateResource();

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
        OriginalCertificate originalCertificate = new OriginalCertificate();
        originalCertificate.setCertificate(certificate);
        when(txManager.getTransaction((TransactionDefinition) anyObject())).thenReturn(txStatus);
        when(moduleRegistry.getModuleApi(Mockito.anyString())).thenReturn(moduleApi);
        certificateResource.insertCertificate(ConverterUtil.toCertificateHolder(certificate));

        ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
        verify(entityManager, Mockito.times(2)).persist(argument.capture());
        List<Object> persistedObjects = argument.getAllValues();
        Certificate certificateArgument = (Certificate) persistedObjects.get(0);
        Assert.assertEquals(certificate.getId(), certificateArgument.getId());
        OriginalCertificate originalCertificateArgument = (OriginalCertificate) persistedObjects.get(1);
        Assert.assertEquals(certificate.getId(), originalCertificateArgument.getCertificate().getId());
        verify(txManager).commit(txStatus);
    }

    @Test
    public void testInsertCertificateHandlesException() throws Exception {
        Certificate certificate = new Certificate("1", "");
        when(txManager.getTransaction((TransactionDefinition) anyObject())).thenReturn(txStatus);
        doThrow(new RuntimeException("")).when(entityManager).persist(Mockito.any(Certificate.class));

        certificateResource.insertCertificate(ConverterUtil.toCertificateHolder(certificate));

        verify(txStatus).setRollbackOnly();
    }
}
