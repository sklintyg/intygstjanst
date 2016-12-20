/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import static se.inera.intyg.intygstjanst.web.support.CertificateForSjukfallFactory.getFactoryInstance;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSjukfallCertificateConverter;

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
        Certificate certificate = getFactoryInstance().buildCert();
        certificate.setType("other");
        testee.created(certificate);
        verifyZeroInteractions(sjukfallCertificateDao);
    }

    @Test
    public void testReturnsFalseIfUnparsableUtlatande() throws ModuleNotFoundException, IOException {
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(any())).thenThrow(IOException.class);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);

        boolean result = testee.created(getFactoryInstance().buildCert());
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).store(any(SjukfallCertificate.class));
    }

    @Test
    public void testNoStoreIfModuleWasntFound() throws ModuleNotFoundException, IOException {

        when(moduleRegistry.getModuleApi(anyString())).thenThrow(ModuleNotFoundException.class);
        boolean result = testee.created(getFactoryInstance().buildCert());
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).store(any(SjukfallCertificate.class));
    }

    @Test
    public void testNoStoreIfNoDiagnosKod() throws ModuleNotFoundException, IOException {
        Fk7263Utlatande utlatande = getFactoryInstance().buildUtlatande();
        utlatande.setDiagnosKod(null);
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(any())).thenReturn(utlatande);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);

        boolean result = testee.created(getFactoryInstance().buildCert());
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).store(any(SjukfallCertificate.class));
    }

    @Test
    public void testOk() throws ModuleNotFoundException, IOException {
        Fk7263Utlatande utlatande = getFactoryInstance().buildUtlatande();
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(any())).thenReturn(utlatande);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(true);

        boolean result = testee.created(getFactoryInstance().buildCert());
        assertTrue(result);
        verify(sjukfallCertificateDao, times(1)).store(any(SjukfallCertificate.class));
    }

    @Test
    public void testRevokeNotFk7263() throws ModuleNotFoundException, IOException {
        Certificate certificate = getFactoryInstance().buildCert();
        certificate.setType("other");
        boolean revoked = testee.revoked(certificate);
        assertFalse(revoked);
        verify(sjukfallCertificateDao, times(0)).revoke(anyString());
    }

    @Test
    public void testRevoke() throws ModuleNotFoundException, IOException {
        Fk7263Utlatande utlatande = getFactoryInstance().buildUtlatande();
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(any())).thenReturn(utlatande);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(true);
        boolean revoked = testee.revoked(getFactoryInstance().buildCert());
        assertTrue(revoked);
        verify(sjukfallCertificateDao).revoke(anyString());
    }

    @Test
    public void testRevokeReturnsFalseIfUnparsableUtlatande() throws ModuleNotFoundException, IOException {
        Fk7263Utlatande utlatande = getFactoryInstance().buildUtlatande();
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(any())).thenReturn(utlatande);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);

        boolean result = testee.created(getFactoryInstance().buildCert());
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).revoke(anyString());
    }
}
