/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSjukfallCertificateConverter;

@RunWith(MockitoJUnitRunner.StrictStubs.Silent.class)
public class SjukfallCertificateServiceImplTest {

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
    public void testDoesNothingIfNotFk7263() {
        Certificate certificate = getFactoryInstance().buildCert("other");
        certificate.setType("other");
        testee.created(certificate);
        verifyNoInteractions(sjukfallCertificateDao);
    }

    @Test
    public void testReturnsFalseIfUnparsableUtlatande() throws ModuleNotFoundException, IOException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);

        boolean result = testee.created(getFactoryInstance().buildCert(INTYG_TYPE_FK7263));
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).store(any(SjukfallCertificate.class));
    }

    @Test
    public void testNoStoreIfModuleWasntFound() throws ModuleNotFoundException, IOException {

        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenThrow(ModuleNotFoundException.class);
        boolean result = testee.created(getFactoryInstance().buildCert(INTYG_TYPE_FK7263));
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).store(any(SjukfallCertificate.class));
    }

    @Test
    public void testNoStoreIfNoDiagnosKod() throws ModuleNotFoundException, IOException {
        Fk7263Utlatande utlatande = getFactoryInstance().buildFk7263Utlatande();
        utlatande.setDiagnosKod(null);
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);

        boolean result = testee.created(getFactoryInstance().buildCert(INTYG_TYPE_FK7263));
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).store(any(SjukfallCertificate.class));
    }

    @Test
    public void testFk7263Ok() throws ModuleNotFoundException, IOException {
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
    public void testLisjpOk() throws ModuleNotFoundException, IOException {
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
    public void testRevokeNotFk7263() throws ModuleNotFoundException, IOException {
        Certificate certificate = getFactoryInstance().buildCert(INTYG_TYPE_FK7263);
        certificate.setType("other");
        boolean revoked = testee.revoked(certificate);
        assertFalse(revoked);
        verify(sjukfallCertificateDao, times(0)).revoke(anyString());
    }

    @Test
    public void testRevokeSmittskyddLisjp() throws ModuleNotFoundException, IOException {
        Certificate certificate = getFactoryInstance().buildCert(INTYG_TYPE_LISJP);

        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableLisjp(any())).thenReturn(false);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);
        boolean revoked = testee.revoked(certificate);
        assertFalse(revoked);
        verify(sjukfallCertificateDao, times(0)).revoke(anyString());
    }

    @Test
    public void testRevokeFk7263() throws ModuleNotFoundException, IOException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(true);
        boolean revoked = testee.revoked(getFactoryInstance().buildCert(INTYG_TYPE_FK7263));
        assertTrue(revoked);
        verify(sjukfallCertificateDao).revoke(anyString());
    }

    @Test
    public void testRevokeLisjp() throws ModuleNotFoundException, IOException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);
        when(certificateToSjukfallCertificateConverter.isConvertableLisjp(any())).thenReturn(true);
        boolean revoked = testee.revoked(getFactoryInstance().buildCert(INTYG_TYPE_LISJP));
        assertTrue(revoked);
        verify(sjukfallCertificateDao).revoke(anyString());
    }

    @Test
    public void testRevokeReturnsFalseIfUnparsableUtlatande() throws ModuleNotFoundException, IOException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(certificateToSjukfallCertificateConverter.isConvertableFk7263(any())).thenReturn(false);

        boolean result = testee.created(getFactoryInstance().buildCert(INTYG_TYPE_FK7263));
        assertFalse(result);
        verify(sjukfallCertificateDao, times(0)).revoke(anyString());
    }
}
