package se.inera.intyg.intygstjanst.web.service;

import java.time.LocalDate;
import java.util.List;
import se.inera.intyg.infra.certificate.dto.DiagnosedCertificate;
import se.inera.intyg.infra.certificate.dto.SickLeaveCertificate;
import se.inera.intyg.schemas.contract.Personnummer;

public interface TypedCertificateService {

    List<DiagnosedCertificate> listDiagnosedCertificatesForCareUnits(List<String> units, List<String> certificateTypeList,
        LocalDate fromDate, LocalDate toDate);

    List<DiagnosedCertificate> listDiagnosedCertificatesForPerson(Personnummer personId, List<String> certificateTypeList,
        LocalDate fromDate, LocalDate toDate, List<String> units);

    List<SickLeaveCertificate> listSickLeaveCertificatesForPerson(Personnummer personId, List<String> certificateTypeList,
        LocalDate fromDate, LocalDate toDate, List<String> units);
}
