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
package se.inera.intyg.intygstjanst.web.integration.testability.util;


import jakarta.xml.bind.JAXB;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.web.integration.testability.TestabilityRegisterCertificate;
import se.inera.intyg.intygstjanst.web.integration.testability.TestabilityRevokeCertificate;
import se.inera.intyg.intygstjanst.web.integration.testability.TestabilitySendCertificate;
import se.inera.intyg.intygstjanst.web.integration.testability.dto.TestabilityConfigProvider;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;

@Component
public class IntegrationTestUtil {

    private static final String BASE_URL = "testability/register/";
    private final TestabilityRegisterCertificate testabilityRegisterCertificate;
    private final TestabilityRevokeCertificate testabilityRevokeCertificate;
    private final TestabilitySendCertificate testabilitySendCertificate;

    public IntegrationTestUtil(
        TestabilityRegisterCertificate testabilityRegisterCertificate, TestabilityRevokeCertificate testabilityRevokeCertificate,
        TestabilitySendCertificate testabilitySendCertificate) {
        this.testabilityRegisterCertificate = testabilityRegisterCertificate;
        this.testabilityRevokeCertificate = testabilityRevokeCertificate;
        this.testabilitySendCertificate = testabilitySendCertificate;
    }

    public void registerCertificateTestabilityCreate(TestabilityConfigProvider testabilityConfigProvider) {
        final var filePath = getFilePath();
        final var templateGroup = new STGroupFile(filePath);
        final var relation = testabilityConfigProvider.getRelationsId() != null
            ? getRelation(testabilityConfigProvider.getRelationsId(), templateGroup, testabilityConfigProvider.getRelationKod()) : "";
        final var workCapacities = getWorkCapacities(testabilityConfigProvider, templateGroup);
        final var diagnosisCodes = getDiagnosis(testabilityConfigProvider.getDiagnosisCode(), templateGroup);
        ST requestTemplate = templateGroup.getInstanceOf("requestParameterizedTestabilityCreate");
        addFieldsFromTestabilityConfigProvider(requestTemplate, testabilityConfigProvider, relation, workCapacities, diagnosisCodes);
        executeRegisterCertificate(requestTemplate);
        updateCertificateStatus(testabilityConfigProvider);
    }

    private static List<String> getWorkCapacities(TestabilityConfigProvider testabilityConfigProvider, STGroupFile templateGroup) {
        return testabilityConfigProvider.getWorkCapacity().stream()
            .map(capacity ->
                getWorkCapacity(
                    capacity,
                    templateGroup,
                    testabilityConfigProvider.getFromDays(),
                    testabilityConfigProvider.getToDays(),
                    testabilityConfigProvider.getWorkCapacity().indexOf(capacity))
            )
            .collect(Collectors.toList());
    }

    private void updateCertificateStatus(TestabilityConfigProvider testabilityConfigProvider) {
        if (testabilityConfigProvider.isSend()) {
            testabilitySendCertificate.sendCertificate(testabilityConfigProvider.getPatientId(),
                testabilityConfigProvider.getCertificateId());
        }
        if (testabilityConfigProvider.isRevoked()) {
            testabilityRevokeCertificate.revokeCertificate(testabilityConfigProvider.getPatientId(),
                testabilityConfigProvider.getCertificateId());
        }
    }

    private static void addFieldsFromTestabilityConfigProvider(ST requestTemplate, TestabilityConfigProvider testabilityConfigProvider,
        String relation, List<String> workCapacities, String diagnosisCode) {
        requestTemplate.add("intygId", testabilityConfigProvider.getCertificateId());
        requestTemplate.add("personId", testabilityConfigProvider.getPatientId());
        if (!relation.isEmpty()) {
            requestTemplate.add("relation", relation);
        }
        requestTemplate.add("doctorId", testabilityConfigProvider.getDoctorId());
        requestTemplate.add("careProviderId", testabilityConfigProvider.getCareProviderId());
        requestTemplate.add("unitId", testabilityConfigProvider.getCareUnitId());
        requestTemplate.add("doctorName", testabilityConfigProvider.getDoctorName());
        requestTemplate.add("diagnosisCodes", diagnosisCode);
        requestTemplate.add("workCapacity", workCapacities);
        requestTemplate.add("occupation", testabilityConfigProvider.getOccupation());
        requestTemplate.add("signedAndSentDateTime", dateTimeAsStr(testabilityConfigProvider.getSignTimestamp()));
        requestTemplate.add("careUnitName", testabilityConfigProvider.getCareUnitName());
        applyToFromDatesToRequestTemplate(requestTemplate, testabilityConfigProvider.getFromDays(), testabilityConfigProvider.getToDays());
    }

    private static String dateTimeAsStr(LocalDateTime localDateTime) {
        return localDateTime.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private static String getRelation(String relationId, STGroupFile templateGroup, RelationKod relationKod) {
        final var requestTemplate = templateGroup.getInstanceOf("requestParameterizedRelation");
        requestTemplate.add("relationCode", relationKod);
        requestTemplate.add("relationName", relationKod.getKlartext());
        requestTemplate.add("relationId", relationId);
        return requestTemplate.render();
    }

    private static String getWorkCapacity(String workCapacity, STGroupFile templateGroup, int fromDays, int toDays, int instance) {
        final var requestTemplate = templateGroup.getInstanceOf("requestGetWorkCapacity");
        requestTemplate.add("workCapacity", workCapacity);
        requestTemplate.add("instance", instance);
        applyToFromDatesToRequestTemplate(requestTemplate, fromDays, toDays);
        return requestTemplate.render();
    }

    private static String getDiagnosis(List<String> diagnosisCodes, STGroupFile templateGroup) {
        final var requestTemplate = templateGroup.getInstanceOf("requestGetDiagnosisCode");
        final var templates = List.of("mainDiagnosisCode", "secondDiagnosisCode", "thirdDiagnosisCode");
        for (int i = 0; i < diagnosisCodes.size(); i++) {
            requestTemplate.add(templates.get(i), diagnosisCodes.get(i));
        }
        return requestTemplate.render();
    }

    private void executeRegisterCertificate(ST requestTemplate) {
        RegisterCertificateType registerCertificateType =
            JAXB.unmarshal(new StringReader(requestTemplate.render()), RegisterCertificateType.class);
        testabilityRegisterCertificate.registerCertificate(registerCertificateType);
    }

    private static void applyToFromDatesToRequestTemplate(ST requestTemplate, int fromDaysRelativeToNow, int toDaysRelativeToNow) {
        requestTemplate.add("fromDate", LocalDate.now().plusDays(fromDaysRelativeToNow).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        requestTemplate.add("toDate", LocalDate.now().plusDays(toDaysRelativeToNow).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    private static String getFilePath() {
        return BASE_URL + "request_testability.stg";
    }
}
