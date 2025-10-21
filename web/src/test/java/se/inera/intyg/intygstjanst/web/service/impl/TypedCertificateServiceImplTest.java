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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.certificate.dto.DiagnosedCertificate;
import se.inera.intyg.infra.certificate.dto.SickLeaveCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.web.csintegration.GetSickLeaveCertificatesFromCS;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToDiagnosedCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSickLeaveCertificateConverter;
import se.inera.intyg.schemas.contract.Personnummer;

@ExtendWith(MockitoExtension.class)
class TypedCertificateServiceImplTest {

    private static final String CERT_ID = "cert-123";
    private static final LocalDateTime CERT_SIGNING_DATETIME = LocalDateTime.parse("2016-02-01T15:00:00");
    private static final String PERSONNUMMER = "19121212-1212";
    private static final String DOC_NAME = "Doc Name";
    private static final String CARE_UNIT_ID = "enhet-1";
    private static final String CARE_UNIT_NAME = "Enhet1";
    private static final String CARE_GIVER_ID = "vardgivare-1";
    private static final String CERT_TYPE_AG7804 = "ag7804";
    private static final String CERT_TYPE_LUSE = "luse";

    private final Personnummer pNr = Personnummer.createPersonnummer(PERSONNUMMER).orElseThrow();

    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private CertificateDao certificateDao;
    @Mock
    private CertificateToDiagnosedCertificateConverter diagnosedCertificateConverter;
    @Mock
    private CertificateToSickLeaveCertificateConverter sickLeaveCertificateConverter;
    @Mock
    private GetSickLeaveCertificatesFromCS getSickLeaveCertificatesFromCS;
    @InjectMocks
    private TypedCertificateServiceImpl typedCertificateService;

    private final ModuleApi moduleApi = mock(ModuleApi.class);

    @Test
    void listDiagnosedCertificatesForPerson() throws ModuleNotFoundException, ModuleException {
        List<Certificate> certificates = Collections.singletonList(buildCertificate(CERT_TYPE_LUSE));
        when(certificateDao.findCertificate(any(), any(), any(), any(), any())).thenReturn(certificates);
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromXml(anyString())).thenReturn(null);
        when(diagnosedCertificateConverter.convertLuse(any(), any())).thenReturn(new DiagnosedCertificate());

        var diagnosedCertificates = typedCertificateService
            .listDiagnosedCertificatesForPerson(pNr, Collections.singletonList(CERT_TYPE_LUSE), null,
                null, Collections.singletonList(CARE_UNIT_ID));

        assertNotNull(diagnosedCertificates);
        assertEquals(1, diagnosedCertificates.size());
    }

    @Test
    void listSickLeaveCertificatesForPerson() throws ModuleNotFoundException, ModuleException {
        final var expected = List.of(new SickLeaveCertificate(), new SickLeaveCertificate());
        
        final var certificates = Collections.singletonList(buildCertificate(CERT_TYPE_AG7804));
        when(certificateDao.findCertificate(any(), any(), any(), any(), any())).thenReturn(certificates);
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromXml(anyString())).thenReturn(null);
        when(sickLeaveCertificateConverter.convertAg7804(any(), any())).thenReturn(new SickLeaveCertificate());
        when(getSickLeaveCertificatesFromCS.get(any(), any(), any(), any(), any())).thenReturn(List.of(new SickLeaveCertificate()));

        final var actual = typedCertificateService.listSickLeaveCertificatesForPerson(pNr, Collections.singletonList(CERT_TYPE_LUSE), null,
            null, Collections.singletonList(CARE_UNIT_ID));

        assertEquals(expected, actual);
    }

    @Test
    void shallUseQueryWithMetaDataForFindingDiagnosCertificates() {
        when(certificateDao.findCertificatesUsingMetaDataTable(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

        typedCertificateService.listDiagnosedCertificatesForCareUnits(Collections.singletonList(CARE_UNIT_ID),
            Arrays.asList(CERT_TYPE_LUSE, CERT_TYPE_AG7804), null, null, Collections.emptyList());

        verify(certificateDao, times(1)).findCertificatesUsingMetaDataTable(any(), any(), any(), any(), any());
    }

    @Test
    void shallUseQueryWithMetaDataForFindingDoctors() {
        when(certificateDao.findDoctorIds(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        typedCertificateService
            .listDoctorsForCareUnits(Collections.singletonList(CARE_UNIT_ID), Arrays.asList(CERT_TYPE_LUSE, CERT_TYPE_AG7804), null, null);

        verify(certificateDao, times(1)).findDoctorIds(any(), any(), any(), any());
    }

    private Certificate buildCertificate(String type) {
        var certificate = new Certificate(CERT_ID);
        certificate.setType(type);
        certificate.setTypeVersion("1.0");
        certificate.setSignedDate(CERT_SIGNING_DATETIME);
        certificate.setSigningDoctorName(DOC_NAME);
        certificate.setCivicRegistrationNumber(pNr);
        certificate.setCareGiverId(CARE_GIVER_ID);
        certificate.setCareUnitId(CARE_UNIT_ID);
        certificate.setCareUnitName(CARE_UNIT_NAME);
        certificate.setOriginalCertificate(new OriginalCertificate(LocalDateTime.now(), "XML", certificate));
        return certificate;
    }
}
