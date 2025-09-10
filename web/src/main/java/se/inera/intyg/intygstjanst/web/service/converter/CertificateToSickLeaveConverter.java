package se.inera.intyg.intygstjanst.web.service.converter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCode;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCodeList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRange;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRangeList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosis;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosisList;
import se.inera.intyg.intygstjanst.persistence.model.builder.SjukfallCertificateBuilder;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;

@Component
public class CertificateToSickLeaveConverter {

    private static final String FK_7804_TYPE = "fk7804";
    private static final String QUESTION_DIAGNOSIS_ID = "6";
    private static final String QUESTION_NEDSATTNING_ARBETSFORMAGA_ID = "32";
    private static final String QUESTION_SYSSELSATTNING_ID = "28";
    private static final String MISSING_ELEMENT_IN_CERTIFICATE = "Could not find data element %s in certificate";
    private static final String LISJP = "lisjp";

    public SjukfallCertificate convert(Certificate certificate) {
        final var metadata = certificate.getMetadata();
        final var sickLeaveCertificate = new SjukfallCertificateBuilder(metadata.getId())
            .careGiverId(metadata.getCareProvider().getUnitId())
            .careUnitId(metadata.getCareUnit().getUnitId())
            .careUnitName(metadata.getCareUnit().getUnitName())
            .certificateType(toSjukfallCertificateType(metadata.getType()))
            .civicRegistrationNumber(metadata.getPatient().getPersonId().getId())
            .signingDoctorName(metadata.getIssuedBy().getFullName())
            .patientName(metadata.getPatient().getFullName())
            .diagnoseCode(getDiagnoses(certificate).getFirst().getCode())
            .signingDoctorId(metadata.getIssuedBy().getPersonId())
            .signingDateTime(metadata.getSigned())
            .deleted(metadata.getRevokedAt() != null)
            .workCapacities(buildWorkCapacities(certificate.getData()))
            .employment(buildEmployment(certificate.getData()))
            .testCertificate(metadata.isTestCertificate())
            .build();

        sickLeaveCertificate.setBiDiagnoseCode1(getDiagnoses(certificate).size() > 1 ? getDiagnoses(certificate).get(1).getCode() : null);
        sickLeaveCertificate.setBiDiagnoseCode2(getDiagnoses(certificate).size() > 2 ? getDiagnoses(certificate).get(2).getCode() : null);

        return sickLeaveCertificate;
    }

    private String buildEmployment(Map<String, CertificateDataElement> data) {
        final var codeList = data.entrySet().stream()
            .filter(e -> QUESTION_SYSSELSATTNING_ID.equals(e.getKey()))
            .findFirst()
            .map(Entry::getValue)
            .map(CertificateDataElement::getValue)
            .map(CertificateDataValueCodeList.class::cast)
            .map(CertificateDataValueCodeList::getList)
            .orElseThrow(
                () -> new IllegalStateException(MISSING_ELEMENT_IN_CERTIFICATE.formatted(QUESTION_SYSSELSATTNING_ID)));

        return codeList.stream()
            .map(CertificateDataValueCode::getId)
            .collect(Collectors.joining(","));
    }

    private List<SjukfallCertificateWorkCapacity> buildWorkCapacities(Map<String, CertificateDataElement> data) {
        final var workCapacities = new ArrayList<SjukfallCertificateWorkCapacity>();
        final var dateRanges = data.entrySet().stream()
            .filter(e -> QUESTION_NEDSATTNING_ARBETSFORMAGA_ID.equals(e.getKey()))
            .findFirst()
            .map(Entry::getValue)
            .map(CertificateDataElement::getValue)
            .map(CertificateDataValueDateRangeList.class::cast)
            .map(CertificateDataValueDateRangeList::getList)
            .orElseThrow(
                () -> new IllegalStateException(MISSING_ELEMENT_IN_CERTIFICATE.formatted(QUESTION_NEDSATTNING_ARBETSFORMAGA_ID)));

        for (CertificateDataValueDateRange dateRange : dateRanges) {
            final var workCapacity = new SjukfallCertificateWorkCapacity();
            workCapacity.setCapacityPercentage(toCapacityPercentage(dateRange.getId()));
            workCapacity.setFromDate(dateRange.getFrom().format(DateTimeFormatter.ISO_LOCAL_DATE));
            workCapacity.setToDate(dateRange.getTo().format(DateTimeFormatter.ISO_LOCAL_DATE));
            workCapacities.add(workCapacity);
        }

        return workCapacities;
    }

    private static List<CertificateDataValueDiagnosis> getDiagnoses(Certificate certificate) {
        return certificate.getData().entrySet().stream()
            .filter(e -> QUESTION_DIAGNOSIS_ID.equals(e.getKey()))
            .findFirst()
            .map(Entry::getValue)
            .map(CertificateDataElement::getValue)
            .map(CertificateDataValueDiagnosisList.class::cast)
            .map(CertificateDataValueDiagnosisList::getList)
            .orElseThrow(
                () -> new IllegalStateException(MISSING_ELEMENT_IN_CERTIFICATE.formatted(QUESTION_DIAGNOSIS_ID)));
    }

    private Integer toCapacityPercentage(String id) {
        return switch (SickLeaveCapacity.valueOf(id)) {
            case HELT_NEDSATT -> 100;
            case TRE_FJARDEDEL -> 75;
            case HALFTEN -> 50;
            case EN_FJARDEDEL -> 25;
        };
    }

    private String toSjukfallCertificateType(String type) {
        if (FK_7804_TYPE.equals(type)) {
            return LISJP;
        }

        throw new IllegalStateException("Unknown certificate type: " + type);
    }

    enum SickLeaveCapacity {
        HELT_NEDSATT, TRE_FJARDEDEL, HALFTEN, EN_FJARDEDEL
    }
}