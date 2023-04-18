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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.integration.testability.dto.TestabilityConfigProvider;
import se.inera.intyg.intygstjanst.web.integration.testability.util.IntegrationTestUtil;
import se.inera.intyg.intygstjanst.web.service.TestabilityService;

@Service
public class TestabilityServiceImpl implements TestabilityService {

    private final HsaService hsaService;
    public static final String ATHENA_ANDERSSON_ID = "194011306125";
    public static final String ALVE_ALFRIDSSON_ID = "194112128154";
    public static final String BOSTADSLOSE_ANDERSSON_ID = "194110147495";
    public static final String ATLAS_ABRAHAMSSON_ID = "194111299055";
    public static final String ANONYMA_ATTILA_ID = "194012019149";
    public static final String ALEXA_VALFRIDSSON = "194110299221";
    private static final String DEFAULT_RELATIONS_ID = null;
    private static final RelationKod DEFAULT_RELATIONS_KOD = null;
    private static final String DIAGNOSIS_CODE_A010 = "A010";
    private static final String DIAGNOSIS_CODE_F430 = "F430";
    private static final String DIAGNOSIS_CODE_K23 = "K23";
    private static final String DIAGNOSIS_CODE_Z010 = "Z010";
    private static final String DIAGNOSIS_CODE_P23 = "P23";
    private static final String DOKTOR_AJLA = "TSTNMT2321000156-DRAA";
    private static final String DOKTOR_ALF = "TSTNMT2321000156-DRAF";
    public static final String ALFA_MEDICINCENTRUM = "TSTNMT2321000156-ALMC";
    public static final String ALFA_REGIONEN = "TSTNMT2321000156-ALFA";
    public static final String ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN = "TSTNMT2321000156-ALIM";
    private static final String DEGREE_25 = "EN_FJARDEDEL";
    private static final String DEGREE_75 = "TRE_FJARDEDEL";
    private static final String DEGREE_100 = "HELT_NEDSATT";
    private static final String DEGREE_50 = "HALFTEN";
    private final IntegrationTestUtil integrationTestUtil;

    private static final String NUVARANDE_ARBETE = "NUVARANDE_ARBETE";
    private static final String ARBETSSOKANDE = "ARBETSSOKANDE";
    private static final String FORADLRARLEDIGHET_VARD_AV_BARN = "FORALDRALEDIG";
    private static final String STUDIER = "STUDIER";

    public TestabilityServiceImpl(HsaService hsaService, IntegrationTestUtil integrationTestUtil) {
        this.hsaService = hsaService;
        this.integrationTestUtil = integrationTestUtil;
    }

    @Override
    public void createDefaultTestData() {
        testabilityConfigProviderList().forEach(integrationTestUtil::registerCertificateTestabilityCreate);
    }

    private List<TestabilityConfigProvider> testabilityConfigProviderList() {
        final var testabilityConfigProviders = new ArrayList<TestabilityConfigProvider>();
        testabilityConfigProviders.addAll(getAthenaAndersson());
        testabilityConfigProviders.addAll(getBostadslosaAndersson());
        testabilityConfigProviders.addAll(getAlveAlfridsson());
        testabilityConfigProviders.addAll(getDeceasedAtlas());
        testabilityConfigProviders.addAll(getValidationPatientAlexa());
        testabilityConfigProviders.addAll(getAnonymaAttila());
        return testabilityConfigProviders;
    }

    private List<TestabilityConfigProvider> getAthenaAndersson() {
        return List.of(
            getConfig(ATHENA_ANDERSSON_ID, -10, 20, DIAGNOSIS_CODE_A010, DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, DEGREE_50),
            getConfig(ATHENA_ANDERSSON_ID, -42, -12, DIAGNOSIS_CODE_A010, DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, DEGREE_75),
            getConfig(ATHENA_ANDERSSON_ID, -75, -45, DIAGNOSIS_CODE_A010, DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_ALF, ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN, DEGREE_100),
            getConfig(ATHENA_ANDERSSON_ID, -130, -100, DIAGNOSIS_CODE_Z010, DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_ALF, ALFA_MEDICINCENTRUM, DEGREE_100),
            getConfig(ATHENA_ANDERSSON_ID, -160, -130, DIAGNOSIS_CODE_Z010, DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, DEGREE_100),
            getConfig(ATHENA_ANDERSSON_ID, -195, -165, DIAGNOSIS_CODE_Z010, DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_ALF, ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN, DEGREE_75),
            getConfig(ATHENA_ANDERSSON_ID, -230, -200, DIAGNOSIS_CODE_Z010, DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_ALF, ALFA_MEDICINCENTRUM, DEGREE_75)
        );
    }

    private List<TestabilityConfigProvider> getAlveAlfridsson() {
        return List.of(
            getConfig(ALVE_ALFRIDSSON_ID, -20, 10, DIAGNOSIS_CODE_K23, DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD, getRandomId(),
                FORADLRARLEDIGHET_VARD_AV_BARN, DOKTOR_ALF, ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN, DEGREE_50)
        );
    }

    private List<TestabilityConfigProvider> getBostadslosaAndersson() {
        return List.of(
            getConfig(BOSTADSLOSE_ANDERSSON_ID, -44, 1, DIAGNOSIS_CODE_P23, DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), STUDIER, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, DEGREE_25)
        );
    }

    private List<TestabilityConfigProvider> getAnonymaAttila() {
        return List.of(
            getConfig(ANONYMA_ATTILA_ID, -30, -5, DIAGNOSIS_CODE_F430, DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD, getRandomId(),
                ARBETSSOKANDE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, DEGREE_25)
        );
    }

    private List<TestabilityConfigProvider> getDeceasedAtlas() {
        return List.of(
            getConfig(ATLAS_ABRAHAMSSON_ID, 0, 5, DIAGNOSIS_CODE_F430, DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD, getRandomId(),
                ARBETSSOKANDE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, DEGREE_25)
        );
    }

    private List<TestabilityConfigProvider> getValidationPatientAlexa() {
        return List.of(
            getConfig(ALEXA_VALFRIDSSON, 0, 5, DIAGNOSIS_CODE_F430, DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD, getRandomId(),
                ARBETSSOKANDE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, DEGREE_25)
        );
    }

    private TestabilityConfigProvider getConfig(String patientId, int fromDays, int toDays, String diagnosisCode, String relationId,
        RelationKod relationKod, String certificateId, String occupation, String doctorId, String careUnitId, String workCapacity) {
        final var doctorName = hsaService.getHsaEmployeeName(doctorId);
        return TestabilityConfigProvider.builder()
            .careUnitId(careUnitId)
            .careProviderId(ALFA_REGIONEN)
            .doctorId(doctorId)
            .doctorName(doctorName)
            .certificateId(certificateId)
            .patientId(patientId)
            .fromDays(fromDays)
            .toDays(toDays)
            .diagnosisCode(diagnosisCode)
            .relationsId(relationId)
            .relationKod(relationKod)
            .occupation(occupation)
            .workCapacity(workCapacity)
            .build();
    }

    private String getRandomId() {
        return UUID.randomUUID().toString();
    }
}
