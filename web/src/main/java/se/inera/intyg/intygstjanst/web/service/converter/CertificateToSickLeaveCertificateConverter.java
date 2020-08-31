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
package se.inera.intyg.intygstjanst.web.service.converter;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.ag114.v1.model.internal.Ag114UtlatandeV1;
import se.inera.intyg.common.ag7804.model.internal.Sjukskrivning;
import se.inera.intyg.common.ag7804.model.internal.Sjukskrivning.SjukskrivningsGrad;
import se.inera.intyg.common.ag7804.v1.model.internal.Ag7804UtlatandeV1;
import se.inera.intyg.common.agparent.model.internal.Diagnos;
import se.inera.intyg.common.support.model.InternalLocalDateInterval;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.infra.certificate.builder.SickLeaveCertificateBuilder;
import se.inera.intyg.infra.certificate.dto.SickLeaveCertificate;
import se.inera.intyg.infra.certificate.dto.SickLeaveCertificate.WorkCapacity;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;

@Component
public class CertificateToSickLeaveCertificateConverter {

    private static final int WORK_CAPACITY_REDUCTION_100 = 100;
    private static final int WORK_CAPACITY_REDUCTION_75 = 75;
    private static final int WORK_CAPACITY_REDUCTION_50 = 50;
    private static final int WORK_CAPACITY_REDUCTION_25 = 25;

    public SickLeaveCertificate convertAg7804(Certificate certificate,
        Utlatande statement) {

        if (!(statement instanceof Ag7804UtlatandeV1)) {
            throw new IllegalArgumentException("Cannot convert " + statement.getClass().getName() + " to SickLeaveCertificate");
        }

        var typedStatement = (Ag7804UtlatandeV1) statement;

        return buildSickLeaveCertificateCommon(certificate, typedStatement.getGrundData(), typedStatement.getDiagnoser())
            .workCapacityList(buildWorkCapacityList(typedStatement.getSjukskrivningar()))
            .occupation(typedStatement.getSysselsattning() != null ? typedStatement.getSysselsattning()
                .stream()
                .filter(Objects::nonNull)
                .map(s -> s.getTyp().getLabel())
                .collect(Collectors.joining(",")) : null)
            .build();
    }

    public SickLeaveCertificate convertAg114(Certificate certificate,
        Utlatande statement) {

        if (!(statement instanceof Ag114UtlatandeV1)) {
            throw new IllegalArgumentException("Cannot convert " + statement.getClass().getName() + " to SickLeaveCertificate");
        }

        var typedStatement = (Ag114UtlatandeV1) statement;

        return buildSickLeaveCertificateCommon(certificate, typedStatement.getGrundData(), typedStatement.getDiagnoser())
            .workCapacityList(buildWorkCapacityList(typedStatement.getSjukskrivningsgrad(),
                Objects.requireNonNull(typedStatement.getSjukskrivningsperiod())))
            .occupation(typedStatement.getSysselsattning() != null ? typedStatement.getSysselsattning()
                .stream()
                .filter(Objects::nonNull)
                .map(s -> s.getTyp().getLabel())
                .collect(Collectors.joining(",")) : null)
            .build();
    }

    private static SickLeaveCertificateBuilder buildSickLeaveCertificateCommon(Certificate certificate, GrundData grundData,
        ImmutableList<Diagnos> diagnoses) {
        return new SickLeaveCertificateBuilder(certificate.getId())
            .certificateType(certificate.getType())
            .personId(certificate.getCivicRegistrationNumber().getPersonnummerWithDash())
            .patientFullName(grundData.getPatient().getFullstandigtNamn())
            .personalHsaId(grundData.getSkapadAv().getPersonId())
            .signingDoctorName(certificate.getSigningDoctorName())
            .careProviderId(certificate.getCareGiverId())
            .careUnitId(certificate.getCareUnitId())
            .careUnitName(certificate.getCareUnitName())
            .signingDateTime(certificate.getSignedDate())
            .deleted(certificate.isRevoked())
            .testCertificate(certificate.isTestCertificate())
            .diagnoseCode((diagnoses != null && !diagnoses.isEmpty()) ? diagnoses.get(0).getDiagnosKod() : null)
            .secondaryDiagnoseCodes(buildSecondaryDiagnoseCodes(diagnoses));
    }


    private static List<WorkCapacity> buildWorkCapacityList(String workCapacityDegree, InternalLocalDateInterval sickLeavePeriod) {
        var workCapacity = new WorkCapacity();
        workCapacity.setReduction(Integer.parseInt(workCapacityDegree));
        workCapacity.setStartDate(sickLeavePeriod.getFrom().asLocalDate());
        workCapacity.setEndDate(sickLeavePeriod.getTom().asLocalDate());
        return Collections.singletonList(workCapacity);
    }

    private static List<WorkCapacity> buildWorkCapacityList(ImmutableList<Sjukskrivning> sickLeaveList) {
        return sickLeaveList.stream().map(CertificateToSickLeaveCertificateConverter::getWorkCapacityReduction)
            .collect(Collectors.toList());
    }

    private static WorkCapacity getWorkCapacityReduction(Sjukskrivning sickLeave) {
        var workCapacity = new WorkCapacity();
        workCapacity.setStartDate(sickLeave.getPeriod().getFrom().asLocalDate());
        workCapacity.setEndDate(sickLeave.getPeriod().getTom().asLocalDate());
        workCapacity.setReduction(getWorkCapacityReduction(sickLeave.getSjukskrivningsgrad()));
        return workCapacity;
    }

    private static int getWorkCapacityReduction(SjukskrivningsGrad workCapacityReduction) {
        switch (workCapacityReduction) {
            case HELT_NEDSATT:
                return WORK_CAPACITY_REDUCTION_100;
            case NEDSATT_3_4:
                return WORK_CAPACITY_REDUCTION_75;
            case NEDSATT_HALFTEN:
                return WORK_CAPACITY_REDUCTION_50;
            case NEDSATT_1_4:
                return WORK_CAPACITY_REDUCTION_25;
        }
        return -1;
    }

    private static List<String> buildSecondaryDiagnoseCodes(ImmutableList<Diagnos> diagnoseList) {
        if (diagnoseList == null || diagnoseList.size() <= 1) {
            return null;
        }

        return diagnoseList.stream().skip(1).map(Diagnos::getDiagnosKod).collect(Collectors.toList());
    }
}
