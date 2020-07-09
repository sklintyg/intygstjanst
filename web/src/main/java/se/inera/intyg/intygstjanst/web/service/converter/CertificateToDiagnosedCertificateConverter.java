package se.inera.intyg.intygstjanst.web.service.converter;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.fkparent.model.internal.Diagnos;
import se.inera.intyg.common.luae_fs.v1.model.internal.LuaefsUtlatandeV1;
import se.inera.intyg.common.luae_na.v1.model.internal.LuaenaUtlatandeV1;
import se.inera.intyg.common.luse.v1.model.internal.LuseUtlatandeV1;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.infra.certificate.builder.DiagnosedCertificateBuilder;
import se.inera.intyg.infra.certificate.dto.DiagnosedCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;

@Component
public class CertificateToDiagnosedCertificateConverter {

    public DiagnosedCertificate convertLuaefs(Certificate certificate,
        Utlatande statement) {

        if (!(statement instanceof LuaefsUtlatandeV1)) {
            throw new IllegalArgumentException("Cannot convert " + statement.getClass().getName() + " to DiagnosedCertificate");
        }

        var typedStatement = (LuaefsUtlatandeV1) statement;

        return getBuild(certificate, typedStatement.getDiagnoser(), typedStatement.getGrundData());
    }

    public DiagnosedCertificate convertLuaena(Certificate certificate,
        Utlatande statement) {

        if (!(statement instanceof LuaenaUtlatandeV1)) {
            throw new IllegalArgumentException("Cannot convert " + statement.getClass().getName() + " to DiagnosedCertificate");
        }

        var typedStatement = (LuaenaUtlatandeV1) statement;

        return getBuild(certificate, typedStatement.getDiagnoser(), typedStatement.getGrundData());
    }

    public DiagnosedCertificate convertLuse(Certificate certificate,
        Utlatande statement) {

        if (!(statement instanceof LuseUtlatandeV1)) {
            throw new IllegalArgumentException("Cannot convert " + statement.getClass().getName() + " to DiagnosedCertificate");
        }

        var typedStatement = (LuseUtlatandeV1) statement;

        return getBuild(certificate, typedStatement.getDiagnoser(), typedStatement.getGrundData());
    }

    private static DiagnosedCertificate getBuild(Certificate certificate, ImmutableList<Diagnos> diagnosis, GrundData grundData) {
        return new DiagnosedCertificateBuilder(certificate.getId())
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
            .diagnoseCode(diagnosis != null ? diagnosis.get(0).getDiagnosKod() : null)
            .secondaryDiagnoseCodes(buildSecondaryDiagnoseCodes(diagnosis))
            .build();
    }

    private static List<String> buildSecondaryDiagnoseCodes(ImmutableList<Diagnos> diagnoseList) {
        if (diagnoseList == null || diagnoseList.size() <= 1) {
            return null;
        }

        return diagnoseList.stream().skip(1).map(Diagnos::getDiagnosKod).collect(Collectors.toList());
    }
}
