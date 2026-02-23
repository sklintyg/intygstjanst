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
package se.inera.intyg.intygstjanst.web.integration.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateMetaData;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.web.integration.converter.ConverterUtil;

@ExtendWith(MockitoExtension.class)
class CertificateResourceTest {

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

    @Mock
    private Utlatande utlatande = mock(Utlatande.class);

    @InjectMocks
    private CertificateResource certificateResource = new CertificateResource();

    @BeforeEach
    void injectTxManager() {
        certificateResource.setTxManager(txManager);
    }

    @Test
    void testGetCertificate() {
        certificateResource.getCertificate("1");

        verify(entityManager).find(Certificate.class, "1");
    }

    @Test
    void testDeleteCertificate() {
        Certificate certificate = new Certificate("1");
        when(txManager.getTransaction(any())).thenReturn(txStatus);
        when(entityManager.find(Certificate.class, "1")).thenReturn(certificate);

        certificateResource.deleteCertificate("1");

        verify(entityManager).find(Certificate.class, "1");
        verify(entityManager).remove(certificate);
        verify(txManager).commit(txStatus);
    }

    @Test
    void testDeleteCertificateHandlesException() {
        Certificate certificate = new Certificate("1");
        when(txManager.getTransaction(any())).thenReturn(txStatus);
        when(entityManager.find(Certificate.class, "1")).thenReturn(certificate);

        doThrow(new RuntimeException("")).when(entityManager).remove(certificate);

        certificateResource.deleteCertificate("1");

        verify(txStatus).setRollbackOnly();
    }

    @Test
    void testInsertCertificate() throws Exception {
        Certificate certificate = new Certificate("1");
        certificate.setOriginalCertificate(new OriginalCertificate(LocalDateTime.now(), "xml", certificate));
        when(txManager.getTransaction(any())).thenReturn(txStatus);
        when(moduleRegistry.getModuleApi(any(), any())).thenReturn(moduleApi);
        when(moduleApi.getAdditionalInfo(any())).thenReturn("additional info");
        when(moduleApi.getUtlatandeFromXml(any())).thenReturn(utlatande);
        GrundData gd = new GrundData();
        HoSPersonal sa = new HoSPersonal();
        sa.setFullstandigtNamn("namn");
        sa.setPersonId("id");
        gd.setSkapadAv(sa);
        when(utlatande.getGrundData()).thenReturn(gd);

        certificateResource.insertCertificate(ConverterUtil.toCertificateHolder(certificate));

        ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
        verify(entityManager, times(3)).persist(argument.capture());
        List<Object> persistedObjects = argument.getAllValues();
        Certificate certificateArgument = (Certificate) persistedObjects.get(0);
        assertEquals(certificate.getId(), certificateArgument.getId());
        CertificateMetaData metadataArgument = (CertificateMetaData) persistedObjects.get(1);
        assertEquals(certificate.getId(), metadataArgument.getCertificateId());
        OriginalCertificate originalCertificateArgument = (OriginalCertificate) persistedObjects.get(2);
        assertEquals(certificate.getId(), originalCertificateArgument.getCertificate().getId());
        verify(txManager).commit(txStatus);
    }

    @Test
    void testInsertCertificateHandlesException() {
        Certificate certificate = new Certificate("1");
        when(txManager.getTransaction(any())).thenReturn(txStatus);

        certificateResource.insertCertificate(ConverterUtil.toCertificateHolder(certificate));

        verify(txStatus).setRollbackOnly();
    }
}
