package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.intygstjanst.web.support.CertificateForSjukfallFactory.getInstance;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSjukfallCertificateConverter;
import se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande;

/**
 * Created by eriklupander on 2016-02-05.
 */
@RunWith(MockitoJUnitRunner.class)
public class SjukfallCertificateServiceImplTest {

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private CertificateToSjukfallCertificateConverter certificateToSjukfallCertificateConverter;

    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;

    @InjectMocks
    private SjukfallCertificateServiceImpl testee;

    private ModuleApi moduleApi = mock(ModuleApi.class);

    @Test
    public void testDoesNothingIfNotFk7263() {
        Certificate certificate = getInstance().buildCert();
        certificate.setType("other");
        testee.created(certificate);
        verifyZeroInteractions(sjukfallCertificateDao);
    }

    @Test
    public void testReturnsFalseIfUnparsableUtlatande() throws ModuleNotFoundException, IOException {
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(any())).thenThrow(IOException.class);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);

        boolean result = testee.created(getInstance().buildCert());
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).store(any(SjukfallCertificate.class));
    }

    @Test
    public void testNoStoreIfModuleWasntFound() throws ModuleNotFoundException, IOException {

        when(moduleRegistry.getModuleApi(anyString())).thenThrow(ModuleNotFoundException.class);
        boolean result = testee.created(getInstance().buildCert());
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).store(any(SjukfallCertificate.class));
    }

    @Test
    public void testNoStoreIfNoDiagnosKod() throws ModuleNotFoundException, IOException {
        Utlatande utlatande = getInstance().buildUtlatande();
        utlatande.setDiagnosKod(null);
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(any())).thenReturn(utlatande);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);

        boolean result = testee.created(getInstance().buildCert());
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).store(any(SjukfallCertificate.class));
    }

    @Test
    public void testOk() throws ModuleNotFoundException, IOException {
        Utlatande utlatande = getInstance().buildUtlatande();
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(any())).thenReturn(utlatande);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(true);

        boolean result = testee.created(getInstance().buildCert());
        assertTrue(result);
        verify(sjukfallCertificateDao, times(1)).store(any(SjukfallCertificate.class));
    }

    @Test
    public void testRevoke() {
        boolean revoked = testee.revoked(getInstance().buildCert());
        assertTrue(revoked);
    }
}
