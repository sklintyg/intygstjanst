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

import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.AGNARSSON_AGNARSSON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALBERTINA_ALISON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALBERT_ALBERTSSON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALBIN_ANDER;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALEXA_VALFRIDSSON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_MEDICINCENTRUM;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN_UNIT_NAME;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_MEDICINCENTRUM_UNIT_NAME;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_REGIONEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALINE_ANDERSSON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALLAN_ALLANSON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALMA_ALMARSSON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALVE_ALFRIDSSON_ID;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ANONYMA_ATTILA_ID;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ARBETSSOKANDE;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ATHENA_ANDERSSON_ID;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ATLAS_ABRAHAMSSON_ID;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BOSTADSLOSE_ANDERSSON_ID;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DEFAULT_RELATIONS_ID;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DEFAULT_RELATIONS_KOD;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DEGREE_100;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DEGREE_25;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DEGREE_50;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DEGREE_75;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_A010;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_F430;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_K23;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_M12;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_N20;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_P23;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_R12;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_Z010;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DOKTOR_AJLA;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DOKTOR_ALF;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.FORADLRARLEDIGHET_VARD_AV_BARN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.INVALID_CODE;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.NUVARANDE_ARBETE;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.STUDIER;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.integration.testability.dto.CreateSickLeaveRequestDTO;
import se.inera.intyg.intygstjanst.web.integration.testability.dto.TestDataOptionsDTO;
import se.inera.intyg.intygstjanst.web.integration.testability.dto.TestabilityConfigProvider;
import se.inera.intyg.intygstjanst.web.integration.testability.util.IntegrationTestUtil;
import se.inera.intyg.intygstjanst.web.integration.testability.util.TestDataProvider;
import se.inera.intyg.intygstjanst.web.service.TestabilityService;

@Service
public class TestabilityServiceImpl implements TestabilityService {

    private final HsaService hsaService;

    private final IntegrationTestUtil integrationTestUtil;

    public TestabilityServiceImpl(HsaService hsaService,
        IntegrationTestUtil integrationTestUtil) {
        this.hsaService = hsaService;
        this.integrationTestUtil = integrationTestUtil;
    }

    @Override
    public void createDefaultTestData() {
        testabilityConfigProviderList().forEach(integrationTestUtil::registerCertificateTestabilityCreate);
    }

    @Override
    public String create(CreateSickLeaveRequestDTO createSickLeaveRequestDTO) {
        final var testabilityConfigProvider = getConfig(
            createSickLeaveRequestDTO.getPatientId(),
            createSickLeaveRequestDTO.getFromDays(),
            createSickLeaveRequestDTO.getToDays(),
            createSickLeaveRequestDTO.getDiagnosisCode(),
            createSickLeaveRequestDTO.getRelationsId(),
            createSickLeaveRequestDTO.getRelationKod(),
            getRandomId(),
            createSickLeaveRequestDTO.getOccupation(),
            createSickLeaveRequestDTO.getDoctorId(),
            createSickLeaveRequestDTO.getCareUnitId(),
            createSickLeaveRequestDTO.getWorkCapacity(),
            createSickLeaveRequestDTO.getCareProviderId(),
            createSickLeaveRequestDTO.isSend(),
            createSickLeaveRequestDTO.isRevoked(),
            createSickLeaveRequestDTO.getSignTimestamp() != null ? createSickLeaveRequestDTO.getSignTimestamp() : LocalDateTime.now(),
            createSickLeaveRequestDTO.getCareUnitName() != null ? createSickLeaveRequestDTO.getCareUnitName()
                : ALFA_MEDICINCENTRUM_UNIT_NAME
        );
        integrationTestUtil.registerCertificateTestabilityCreate(testabilityConfigProvider);
        return testabilityConfigProvider.getCertificateId();
    }

    @Override
    public TestDataOptionsDTO getTestDataOptions() {
        return TestDataOptionsDTO.builder()
            .careProviderIds(
                TestDataProvider.getCareProviders()
            )
            .careUnitIds(
                TestDataProvider.getCareUnits()
            )
            .doctorIds(
                TestDataProvider.getDoctorIds()
            )
            .patientIds(
                TestDataProvider.getPatientIds()
            )
            .relationCodes(
                TestDataProvider.getRelationCodes()
            )
            .diagnosisCodes(
                TestDataProvider.getDiagnosisCodes()
            )
            .occupations(
                TestDataProvider.getOccupations()
            )
            .workCapacity(
                TestDataProvider.getWorkCapacities()
            )
            .build();
    }

    private List<TestabilityConfigProvider> testabilityConfigProviderList() {
        final var testabilityConfigProviders = new ArrayList<TestabilityConfigProvider>();
        testabilityConfigProviders.add(getAlineAndersson());
        testabilityConfigProviders.addAll(getAthenaAndersson());
        testabilityConfigProviders.addAll(getBostadslosaAndersson());
        testabilityConfigProviders.addAll(getAlveAlfridsson());
        testabilityConfigProviders.addAll(getDeceasedAtlas());
        testabilityConfigProviders.addAll(getValidationPatientAlexa());
        testabilityConfigProviders.addAll(getAnonymaAttila());
        testabilityConfigProviders.add(getAgnarssonAgnarsson());
        testabilityConfigProviders.add(getAlbertAlbertsson());
        testabilityConfigProviders.add(getAlbertinaAlison());
        testabilityConfigProviders.add(getAlbinAnder());
        testabilityConfigProviders.add(getAllanAllanson());
        testabilityConfigProviders.add(getAlmaAlmarsson());

        return testabilityConfigProviders;
    }

    private TestabilityConfigProvider getAlmaAlmarsson() {
        return getConfig(ALMA_ALMARSSON, -20, 20, List.of(DIAGNOSIS_CODE_F430, DIAGNOSIS_CODE_M12, DIAGNOSIS_CODE_P23),
            DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
            getRandomId(), ARBETSSOKANDE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, List.of(DEGREE_25, DEGREE_50), ALFA_REGIONEN, false, false,
            LocalDateTime.now(), ALFA_MEDICINCENTRUM_UNIT_NAME);
    }

    private TestabilityConfigProvider getAllanAllanson() {
        return getConfig(ALLAN_ALLANSON, -50, 50, List.of(DIAGNOSIS_CODE_R12), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
            getRandomId(), ARBETSSOKANDE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, List.of(DEGREE_50), ALFA_REGIONEN, false, false,
            LocalDateTime.now(), ALFA_MEDICINCENTRUM_UNIT_NAME);
    }

    private TestabilityConfigProvider getAlineAndersson() {
        return getConfig(ALINE_ANDERSSON, -75, 75, List.of(DIAGNOSIS_CODE_M12), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
            getRandomId(), NUVARANDE_ARBETE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM,
            List.of(DEGREE_75, DEGREE_100), ALFA_REGIONEN, false, false, LocalDateTime.now(), ALFA_MEDICINCENTRUM_UNIT_NAME);
    }

    private TestabilityConfigProvider getAlbinAnder() {
        return getConfig(ALBIN_ANDER, -400, 400, List.of(DIAGNOSIS_CODE_P23), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
            getRandomId(), STUDIER, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, List.of(DEGREE_100), ALFA_REGIONEN, false, false,
            LocalDateTime.now(), ALFA_MEDICINCENTRUM_UNIT_NAME);
    }

    private TestabilityConfigProvider getAlbertinaAlison() {
        return getConfig(ALBERTINA_ALISON, -95, 95, List.of(DIAGNOSIS_CODE_Z010), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
            getRandomId(), STUDIER, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, List.of(DEGREE_25, DEGREE_100), ALFA_REGIONEN, false, false,
            LocalDateTime.now(), ALFA_MEDICINCENTRUM_UNIT_NAME);
    }

    private TestabilityConfigProvider getAlbertAlbertsson() {
        return getConfig(ALBERT_ALBERTSSON, -120, 120, List.of(INVALID_CODE), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
            getRandomId(), FORADLRARLEDIGHET_VARD_AV_BARN, DOKTOR_AJLA, ALFA_MEDICINCENTRUM,
            List.of(DEGREE_25, DEGREE_50, DEGREE_75, DEGREE_100), ALFA_REGIONEN, false, false, LocalDateTime.now(),
            ALFA_MEDICINCENTRUM_UNIT_NAME);
    }

    private TestabilityConfigProvider getAgnarssonAgnarsson() {
        return getConfig(AGNARSSON_AGNARSSON, -200, 200, List.of(DIAGNOSIS_CODE_N20), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
            getRandomId(), NUVARANDE_ARBETE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, List.of(DEGREE_75), ALFA_REGIONEN, false, false,
            LocalDateTime.now(), ALFA_MEDICINCENTRUM_UNIT_NAME);
    }

    private List<TestabilityConfigProvider> getAthenaAndersson() {
        return List.of(
            getConfig(ATHENA_ANDERSSON_ID, -10, 20, List.of(DIAGNOSIS_CODE_A010), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, List.of(DEGREE_50), ALFA_REGIONEN, false, false,
                LocalDateTime.now(), ALFA_MEDICINCENTRUM_UNIT_NAME),
            getConfig(ATHENA_ANDERSSON_ID, -42, -12, List.of(DIAGNOSIS_CODE_A010), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, List.of(DEGREE_75), ALFA_REGIONEN, false, false,
                LocalDateTime.now(), ALFA_MEDICINCENTRUM_UNIT_NAME),
            getConfig(ATHENA_ANDERSSON_ID, -75, -45, List.of(DIAGNOSIS_CODE_A010), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_ALF, ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN, List.of(DEGREE_100),
                ALFA_REGIONEN, false, false, LocalDateTime.now(), ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN_UNIT_NAME),
            getConfig(ATHENA_ANDERSSON_ID, -130, -100, List.of(DIAGNOSIS_CODE_Z010), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_ALF, ALFA_MEDICINCENTRUM, List.of(DEGREE_100), ALFA_REGIONEN, false, false,
                LocalDateTime.now(), ALFA_MEDICINCENTRUM_UNIT_NAME),
            getConfig(ATHENA_ANDERSSON_ID, -160, -130, List.of(DIAGNOSIS_CODE_Z010), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, List.of(DEGREE_100), ALFA_REGIONEN, false, false,
                LocalDateTime.now(), ALFA_MEDICINCENTRUM_UNIT_NAME),
            getConfig(ATHENA_ANDERSSON_ID, -195, -165, List.of(DIAGNOSIS_CODE_Z010), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_ALF, ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN, List.of(DEGREE_75), ALFA_REGIONEN,
                false, false, LocalDateTime.now(), ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN_UNIT_NAME),
            getConfig(ATHENA_ANDERSSON_ID, -230, -200, List.of(DIAGNOSIS_CODE_Z010), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), NUVARANDE_ARBETE, DOKTOR_ALF, ALFA_MEDICINCENTRUM, List.of(DEGREE_75), ALFA_REGIONEN, false, false,
                LocalDateTime.now(), ALFA_MEDICINCENTRUM_UNIT_NAME)
        );
    }

    private List<TestabilityConfigProvider> getAlveAlfridsson() {
        return List.of(
            getConfig(ALVE_ALFRIDSSON_ID, -20, 10, List.of(DIAGNOSIS_CODE_K23), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD, getRandomId(),
                FORADLRARLEDIGHET_VARD_AV_BARN, DOKTOR_ALF, ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN, List.of(DEGREE_50), ALFA_REGIONEN,
                false, false, LocalDateTime.now(), ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN_UNIT_NAME)
        );
    }

    private List<TestabilityConfigProvider> getBostadslosaAndersson() {
        return List.of(
            getConfig(BOSTADSLOSE_ANDERSSON_ID, -44, 1, List.of(DIAGNOSIS_CODE_P23), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD,
                getRandomId(), STUDIER, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, List.of(DEGREE_25), ALFA_REGIONEN, false, false,
                LocalDateTime.now(), ALFA_MEDICINCENTRUM_UNIT_NAME)
        );
    }

    private List<TestabilityConfigProvider> getAnonymaAttila() {
        return List.of(
            getConfig(ANONYMA_ATTILA_ID, -30, -5, List.of(DIAGNOSIS_CODE_F430), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD, getRandomId(),
                ARBETSSOKANDE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, List.of(DEGREE_25), ALFA_REGIONEN, false, false, LocalDateTime.now(),
                ALFA_MEDICINCENTRUM_UNIT_NAME)
        );
    }

    private List<TestabilityConfigProvider> getDeceasedAtlas() {
        return List.of(
            getConfig(ATLAS_ABRAHAMSSON_ID, 0, 5, List.of(DIAGNOSIS_CODE_F430), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD, getRandomId(),
                ARBETSSOKANDE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, List.of(DEGREE_25), ALFA_REGIONEN, false, false, LocalDateTime.now(),
                ALFA_MEDICINCENTRUM_UNIT_NAME)
        );
    }

    private List<TestabilityConfigProvider> getValidationPatientAlexa() {
        return List.of(
            getConfig(ALEXA_VALFRIDSSON, 0, 5, List.of(DIAGNOSIS_CODE_F430), DEFAULT_RELATIONS_ID, DEFAULT_RELATIONS_KOD, getRandomId(),
                ARBETSSOKANDE, DOKTOR_AJLA, ALFA_MEDICINCENTRUM, List.of(DEGREE_25), ALFA_REGIONEN, false, false, LocalDateTime.now(),
                ALFA_MEDICINCENTRUM_UNIT_NAME)
        );
    }

    private TestabilityConfigProvider getConfig(String patientId, int fromDays, int toDays, List<String> diagnosisCode, String relationId,
        RelationKod relationKod, String certificateId, String occupation, String doctorId, String careUnitId, List<String> workCapacity,
        String careProviderId, boolean send, boolean revoked, LocalDateTime signTimestamp, String careUnitName) {
        final var doctorName = hsaService.getHsaEmployeeName(doctorId);
        return TestabilityConfigProvider.builder()
            .careUnitId(careUnitId)
            .careUnitName(careUnitName)
            .careProviderId(careProviderId)
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
            .send(send)
            .revoked(revoked)
            .signTimestamp(signTimestamp)
            .build();
    }

    private String getRandomId() {
        return UUID.randomUUID().toString();
    }
}
