package se.inera.intyg.intygstjanst.web.integration.converter;

import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.builder.ClinicalProcessCertificateMetaTypeBuilder;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.riv.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.riv.clinicalprocess.healthcond.certificate.v1.StatusType;


@Component
public class MetaDataResolver {

    public CertificateMetaType toClinicalProcessCertificateMetaType(
            Certificate source) throws ModuleNotFoundException, ModuleException {

        ClinicalProcessCertificateMetaTypeBuilder builder = new ClinicalProcessCertificateMetaTypeBuilder()
                .certificateId(source.getId())
                .certificateType(source.getType())
                .validity(toLocalDate(source.getValidFromDate()), toLocalDate(source.getValidToDate()))
                .issuerName(source.getSigningDoctorName())
                .facilityName(source.getCareUnitName())
                .signDate(source.getSignedDate())
                .available(source.getDeleted() ? "false" : "true")
                .complemantaryInfo(source.getAdditionalInfo());

        for (CertificateStateHistoryEntry stateEntry : source.getStates()) {
            StatusType status = StatusType.valueOf(stateEntry.getState().name());
            builder.status(status, stateEntry.getTarget(), stateEntry.getTimestamp());
        }

        return builder.build();
    }

    private LocalDate toLocalDate(String date) {
        if (date == null) {
            return null;
        }
        return new LocalDate(date);
    }
}
