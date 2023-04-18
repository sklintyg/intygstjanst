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
package se.inera.intyg.intygstjanst.web.integration.testability.util;


import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.JAXB;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.web.integration.testability.TestabilityRegisterCertificate;
import se.inera.intyg.intygstjanst.web.integration.testability.dto.TestabilityConfigProvider;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;

@Component
public class IntegrationTestUtil {

    private static final String DIAGNOSIS_CODE = "S47";

    private static final String BASE_URL = "testability/register/";
    private final TestabilityRegisterCertificate testabilityRegisterCertificate;

    public IntegrationTestUtil(
        TestabilityRegisterCertificate testabilityRegisterCertificate) {
        this.testabilityRegisterCertificate = testabilityRegisterCertificate;
    }

    public void registerCertificateTestabilityCreate(TestabilityConfigProvider testabilityConfigProvider) {
        String filePath = getFilePath();
        final var templateGroup = new STGroupFile(filePath);
        final var relation = testabilityConfigProvider.getRelationsId() != null
            ? getRelation(testabilityConfigProvider.getRelationsId(), templateGroup, testabilityConfigProvider.getRelationKod()) : "";
        ST requestTemplate = templateGroup.getInstanceOf("requestParameterizedTestabilityCreate");
        addFieldsFromTestabilityConfigProvider(requestTemplate, testabilityConfigProvider, relation);
        executeRegisterCertificate(requestTemplate);
    }

    private static void addFieldsFromTestabilityConfigProvider(ST requestTemplate, TestabilityConfigProvider testabilityConfigProvider,
        String relation) {
        requestTemplate.add("intygId", testabilityConfigProvider.getCertificateId());
        requestTemplate.add("personId", testabilityConfigProvider.getPatientId());
        if (!relation.isEmpty()) {
            requestTemplate.add("relation", relation);
        }
        requestTemplate.add("doctorId", testabilityConfigProvider.getDoctorId());
        requestTemplate.add("careProviderId", testabilityConfigProvider.getCareProviderId());
        requestTemplate.add("unitId", testabilityConfigProvider.getCareUnitId());
        requestTemplate.add("doctorName", testabilityConfigProvider.getDoctorName());
        if (testabilityConfigProvider.getDiagnosisCode() != null) {
            requestTemplate.add("diagnosisCode", testabilityConfigProvider.getDiagnosisCode());
        } else {
            requestTemplate.add("diagnosisCode", DIAGNOSIS_CODE);
        }
        requestTemplate.add("workCapacity", testabilityConfigProvider.getWorkCapacity());
        requestTemplate.add("occupation", testabilityConfigProvider.getOccupation());
        applyToFromDatesToRequestTemplate(requestTemplate, testabilityConfigProvider.getFromDays(), testabilityConfigProvider.getToDays());
    }

    private static String getRelation(String relationId, STGroupFile templateGroup, RelationKod relationKod) {
        final var requestTemplate = templateGroup.getInstanceOf("requestParameterizedRelation");
        requestTemplate.add("relationCode", relationKod);
        requestTemplate.add("relationName", relationKod.getKlartext());
        requestTemplate.add("relationId", relationId);
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
