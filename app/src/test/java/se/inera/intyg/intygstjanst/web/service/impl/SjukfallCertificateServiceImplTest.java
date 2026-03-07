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
package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.intygstjanst.web.support.CertificateForSjukfallFactory.getFactoryInstance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSjukfallCertificateConverter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SjukfallCertificateServiceImplTest {

    public static final String INTYG_TYPE_FK7263 = "fk7263";
    public static final String INTYG_TYPE_LISJP = "lisjp";

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
    void testDoesNothingIfNotFk7263() {
        Certificate certificate = getFactoryInstance().buildCert("other");
        certificate.setType("other");
        testee.created(certificate);
        verifyNoInteractions(sjukfallCertificateDao);
    }

    @Test
    void testReturnsFalseIfUnparsableUtlatande() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);

        boolean result = testee.created(getFactoryInstance().buildCert(INTYG_TYPE_FK7263));
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).store(any(SjukfallCertificate.class));
    }

    @Test
    void testNoStoreIfModuleWasntFound() throws ModuleNotFoundException {

        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenThrow(ModuleNotFoundException.class);
        boolean result = testee.created(getFactoryInstance().buildCert(INTYG_TYPE_FK7263));
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).store(any(SjukfallCertificate.class));
    }

    @Test
    void testNoStoreIfNoDiagnosKod() throws ModuleNotFoundException {
        Fk7263Utlatande utlatande = getFactoryInstance().buildFk7263Utlatande();
        utlatande.setDiagnosKod(null);
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);

        boolean result = testee.created(getFactoryInstance().buildCert(INTYG_TYPE_FK7263));
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).store(any(SjukfallCertificate.class));
    }

    @Test
    void testFk7263Ok() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(true);

        when(certificateToSjukfallCertificateConverter.convertFk7263(
            or(isNull(), any(Certificate.class)),
            or(isNull(), any(Utlatande.class)))
        ).thenReturn(mock(SjukfallCertificate.class));

        boolean result = testee.created(getFactoryInstance().buildCert(INTYG_TYPE_FK7263));
        assertTrue(result);
        verify(sjukfallCertificateDao, times(1)).store(any(SjukfallCertificate.class));
    }

    @Test
    void testLisjpOk() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableLisjp(any())).thenReturn(true);

        when(certificateToSjukfallCertificateConverter.convertLisjp(
            or(isNull(), any(Certificate.class)),
            or(isNull(), any(Utlatande.class)))
        ).thenReturn(mock(SjukfallCertificate.class));

        boolean result = testee.created(getFactoryInstance().buildCert(INTYG_TYPE_LISJP));
        assertTrue(result);
        verify(sjukfallCertificateDao, times(1)).store(any(SjukfallCertificate.class));
    }

    @Test
    void testRevokeNotFk7263() {
        Certificate certificate = getFactoryInstance().buildCert(INTYG_TYPE_FK7263);
        certificate.setType("other");
        boolean revoked = testee.revoked(certificate);
        assertFalse(revoked);
        verify(sjukfallCertificateDao, times(0)).revoke(anyString());
    }

    @Test
    void testRevokeSmittskyddLisjp() throws ModuleNotFoundException {
        Certificate certificate = getFactoryInstance().buildCert(INTYG_TYPE_LISJP);

        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableLisjp(any())).thenReturn(false);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);
        boolean revoked = testee.revoked(certificate);
        assertFalse(revoked);
        verify(sjukfallCertificateDao, times(0)).revoke(anyString());
    }

    @Test
    void testRevokeFk7263() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(true);
        boolean revoked = testee.revoked(getFactoryInstance().buildCert(INTYG_TYPE_FK7263));
        assertTrue(revoked);
        verify(sjukfallCertificateDao).revoke(anyString());
    }

    @Test
    void testRevokeLisjp() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);
        when(certificateToSjukfallCertificateConverter.isConvertableLisjp(any())).thenReturn(true);
        boolean revoked = testee.revoked(getFactoryInstance().buildCert(INTYG_TYPE_LISJP));
        assertTrue(revoked);
        verify(sjukfallCertificateDao).revoke(anyString());
    }

    @Test
    void testRevokeReturnsFalseIfUnparsableUtlatande() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);

        boolean result = testee.created(getFactoryInstance().buildCert(INTYG_TYPE_FK7263));
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).revoke(anyString());
    }
}
