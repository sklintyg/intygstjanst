/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.infra.certificate.dto.CertificateListRequest;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.schemas.contract.Personnummer;

@RunWith(MockitoJUnitRunner.class)
public class CertificateListServiceImplTest {

    private static final String CERT_ID = "cert-123";
    private static final LocalDateTime CERT_SIGNING_DATETIME = LocalDateTime.now().plusMonths(-1);
    private static final String CIVIC_REGISTRATION_NUMBER_STRING = "191212121212";
    private static final String HSA_ID = "doctor-1";
    private static final String CARE_UNIT_ID = "enhet-1";
    private static final String CARE_UNIT_NAME = "Enhet1";
    private static final String CARE_GIVER_ID = "vardgivare-1";
    private static final String CERT_TYPE = "lisjp";
    private static final String CERT_TYPE_VERSION = "1.0";

    @Mock
    private CertificateDao certificateDao;
    @Mock
    private IntygModuleRegistry moduleRegistry;

    @InjectMocks
    CertificateListServiceImpl certificateListService;

    private final ModuleApi moduleApi = mock(ModuleApi.class);
    private final Utlatande convertedCertificate = mock(Utlatande.class);
    private final GrundData basicData = mock(GrundData.class);
    private final HoSPersonal creatorOfCert = mock(HoSPersonal.class);
    private final Patient patient = mock(Patient.class);
    private final ModuleEntryPoint moduleEntryPoint = mock(ModuleEntryPoint.class);


    private final Personnummer CIVIC_REGISTRATION_NUMBER = Personnummer.createPersonnummer(CIVIC_REGISTRATION_NUMBER_STRING).get();


    @Test
    public void getEmptyListOfCertificates() throws ModuleNotFoundException, ModuleException {
        testGetCertificates(true, false);
    }

    @Test
    public void getListOfCertificates() throws ModuleNotFoundException, ModuleException {
        testGetCertificates(false, false);
    }

    @Test
    public void getSortedListOfCertificates() throws ModuleNotFoundException, ModuleException {
        testGetCertificates(false, true);
    }

    private void testGetCertificates(boolean isEmpty, boolean sortList) throws ModuleNotFoundException, ModuleException {
        List<Certificate> certificates;
        CertificateListRequest request = new CertificateListRequest();
        request.setCivicRegistrationNumber(CIVIC_REGISTRATION_NUMBER_STRING);
        request.setUnitId(CARE_UNIT_ID);
        request.setHsaId(HSA_ID);
        request.setStartFrom(0);
        request.setPageSize(10);

        creatorOfCert.setPersonId(HSA_ID);
        basicData.setSkapadAv(creatorOfCert);

        if(sortList) {
            certificates = new ArrayList<>();
            certificates.add(buildCertificate(CERT_TYPE, CERT_ID));
            certificates.add(buildCertificate(CERT_TYPE, CERT_ID + "1"));
            request.setOrderBy("status");
            request.setOrderAscending(true);
        } else if (!isEmpty) {
           certificates = Collections.singletonList(buildCertificate(CERT_TYPE, CERT_ID));
        } else {
            certificates = Collections.emptyList();
        }


        when(patient.getPersonId()).thenReturn(CIVIC_REGISTRATION_NUMBER);
        when(basicData.getPatient()).thenReturn(patient);
        when(creatorOfCert.getPersonId()).thenReturn(HSA_ID);
        when(basicData.getSkapadAv()).thenReturn(creatorOfCert);
        when(convertedCertificate.getTyp()).thenReturn(CERT_TYPE);
        when(convertedCertificate.getTextVersion()).thenReturn(CERT_TYPE_VERSION);
        when(convertedCertificate.getGrundData()).thenReturn(basicData);
        when(moduleEntryPoint.getModuleName()).thenReturn(" ");
        when(moduleRegistry.getModuleEntryPoint(anyString())).thenReturn(moduleEntryPoint);
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromXml(anyString())).thenReturn(convertedCertificate);
        when(certificateDao.findCertificates(CIVIC_REGISTRATION_NUMBER, CARE_UNIT_ID,
            request.getFromDate(), request.getToDate(), request.getOrderBy(), request.isOrderAscending())).thenReturn(certificates);
        var response = certificateListService.listCertificatesForDoctor(request);
        assertEquals(certificates.size(), response.getCertificates().size());
    }

    private Certificate buildCertificate(String type, String certID) {
        var certificate = new Certificate(certID);
        certificate.setType(type);
        certificate.setTypeVersion(CERT_TYPE_VERSION);
        certificate.setSignedDate(CERT_SIGNING_DATETIME);
        certificate.setCivicRegistrationNumber(CIVIC_REGISTRATION_NUMBER);
        certificate.setCareGiverId(CARE_GIVER_ID);
        certificate.setCareUnitId(CARE_UNIT_ID);
        certificate.setCareUnitName(CARE_UNIT_NAME);
        certificate.setOriginalCertificate(new OriginalCertificate(LocalDateTime.now(), "XML", certificate));
        return certificate;
    }
}
