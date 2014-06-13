package se.inera.certificate.integration.converter;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.StatusType;
import se.inera.certificate.integration.builder.ClinicalProcessCertificateMetaTypeBuilder;
import se.inera.certificate.integration.module.ModuleApiFactory;
import se.inera.certificate.integration.module.exception.ModuleNotFoundException;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateStateHistoryEntry;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.exception.ModuleException;

@Component
public class MetaDataResolver {

    @Autowired
    private ModuleApiFactory moduleApiFactory;

    public se.inera.certificate.clinicalprocess.healthcond.certificate.v1.CertificateMetaType toClinicalProcessCertificateMetaType(
            Certificate source) throws ModuleNotFoundException, ModuleException {

        // Get the complementary information from the module
        ModuleEntryPoint module = moduleApiFactory.getModuleEntryPoint(source.getType());
        String complementaryInfo = module.getModuleApi().getComplementaryInfo(new ExternalModelHolder(source.getDocument()));

        ClinicalProcessCertificateMetaTypeBuilder builder = new ClinicalProcessCertificateMetaTypeBuilder()
                .certificateId(source.getId())
                .certificateType(source.getType())
                .validity(toLocalDate(source.getValidFromDate()), toLocalDate(source.getValidToDate()))
                .issuerName(source.getSigningDoctorName())
                .facilityName(source.getCareUnitName())
                .signDate(source.getSignedDate())
                .available(source.getDeleted() ? "false" : "true")
                .complementaryInfo(complementaryInfo);

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
