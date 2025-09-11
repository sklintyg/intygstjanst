package se.inera.intyg.intygstjanst.web.service.converter;

import org.springframework.stereotype.Component;
import se.inera.intyg.intygstjanst.persistence.model.builder.SjukfallCertificateBuilder;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SickLeaveCertificateDTO;

@Component
public class SickLeaveCertificateToSjukfallCertificateConverter {

  public SjukfallCertificate convert(SickLeaveCertificateDTO sickLeaveCertificate) {
    if (sickLeaveCertificate == null) {
      throw new IllegalStateException("Sick leave certificate cannot be null");
    }

    final var sjukfallCertificate = new SjukfallCertificateBuilder(sickLeaveCertificate.getId())
        .certificateType(sickLeaveCertificate.getType())
        .signingDoctorId(sickLeaveCertificate.getSigningDoctorId())
        .signingDoctorName(sickLeaveCertificate.getSigningDoctorName())
        .signingDateTime(sickLeaveCertificate.getSigningDateTime())
        .careUnitId(sickLeaveCertificate.getCareUnitId())
        .careUnitName(sickLeaveCertificate.getCareUnitName())
        .careGiverId(sickLeaveCertificate.getCareGiverId())
        .civicRegistrationNumber(sickLeaveCertificate.getCivicRegistrationNumber())
        .patientName(sickLeaveCertificate.getPatientName())
        .diagnoseCode(sickLeaveCertificate.getDiagnoseCode())
        .employment(sickLeaveCertificate.getEmployment())
        .deleted(sickLeaveCertificate.getDeleted())
        .testCertificate(sickLeaveCertificate.isTestCertificate())
        .workCapacities(sickLeaveCertificate.getSjukfallCertificateWorkCapacity().stream()
            .map(workCapacity -> {
                final var workCapacityEntity = new SjukfallCertificateWorkCapacity();
                workCapacityEntity.setFromDate(workCapacity.getFromDate());
                workCapacityEntity.setToDate(workCapacity.getToDate());
                workCapacityEntity.setCapacityPercentage(workCapacity.getCapacityPercentage());
                return workCapacityEntity;
            }
        ).toList())
        .build();

    sjukfallCertificate.setBiDiagnoseCode1(sickLeaveCertificate.getBiDiagnoseCode1());
    sjukfallCertificate.setBiDiagnoseCode2(sickLeaveCertificate.getBiDiagnoseCode2());

    return sjukfallCertificate;
  }

}
