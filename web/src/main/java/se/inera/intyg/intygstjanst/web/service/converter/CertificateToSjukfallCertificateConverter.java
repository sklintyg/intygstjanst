package se.inera.intyg.intygstjanst.web.service.converter;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Partial;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.model.InternalLocalDateInterval;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.converter.util.PartialConverter;
import se.inera.intyg.intygstjanst.persistence.model.builder.SjukfallCertificateBuilder;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;

/**
 * Converts a (fk7263) Certificate to a CertificateSjukfall.
 *
 * Created by eriklupander on 2016-02-04.
 */
@Component
public class CertificateToSjukfallCertificateConverter {

    private static final int WORK_CAPACITY_100 = 100;
    private static final int WORK_CAPACITY_75 = 75;
    private static final int WORK_CAPACITY_50 = 50;
    private static final int WORK_CAPACITY_25 = 25;

    /**
     * Converts a fk7263 certificate into a SjukfallCertificate.
     *
     * @param certificate
     *      fk7263 cert
     * @param utlatande
     *      fk7263 Utlatande (will perform instanceof check internally)
     * @return
     *      A SjukfallCertificate
     * @throws
     *      IllegalArgumentException if fk7263 chcek fails.
     */
    public SjukfallCertificate convertFk7263(Certificate certificate, Utlatande utlatande) {

        if (!(utlatande instanceof se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande)) {
            throw new IllegalArgumentException("Cannot convert " + utlatande.getClass().getName() + " to SjukfallCertificate");
        }

        se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande fkUtlatande = (se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande) utlatande;

        return new SjukfallCertificateBuilder(certificate.getId())
                .careGiverId(certificate.getCareGiverId())
                .careUnitId(certificate.getCareUnitId())
                .careUnitName(certificate.getCareUnitName())
                .certificateType(certificate.getType())
                .civicRegistrationNumber(certificate.getCivicRegistrationNumber().getPersonnummer())
                .signingDoctorName(certificate.getSigningDoctorName())
                .patientFirstName(fkUtlatande.getGrundData().getPatient().getFornamn())
                .patientLastName(fkUtlatande.getGrundData().getPatient().getEfternamn())
                .diagnoseCode(fkUtlatande.getDiagnosKod())
                .signingDoctorId(fkUtlatande.getGrundData().getSkapadAv().getPersonId())
                .signingDoctorName(fkUtlatande.getGrundData().getSkapadAv().getFullstandigtNamn())
                .certificateType(certificate.getType())
                .workCapacities(buildWorkCapacities(fkUtlatande))
                .build();
    }

    private List<SjukfallCertificateWorkCapacity> buildWorkCapacities(se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande fkUtlatande) {
        List<SjukfallCertificateWorkCapacity> workCapacities = new ArrayList<>();
        if (fkUtlatande.getNedsattMed100() != null) {
            workCapacities.add(buildWorkCapacity(WORK_CAPACITY_100, fkUtlatande.getNedsattMed100()));
        }
        if (fkUtlatande.getNedsattMed75() != null) {
            workCapacities.add(buildWorkCapacity(WORK_CAPACITY_75, fkUtlatande.getNedsattMed75()));
        }
        if (fkUtlatande.getNedsattMed50() != null) {
            workCapacities.add(buildWorkCapacity(WORK_CAPACITY_50, fkUtlatande.getNedsattMed50()));
        }
        if (fkUtlatande.getNedsattMed25() != null) {
            workCapacities.add(buildWorkCapacity(WORK_CAPACITY_25, fkUtlatande.getNedsattMed25()));
        }
        return workCapacities;
    }


    private SjukfallCertificateWorkCapacity buildWorkCapacity(Integer workCapacity, InternalLocalDateInterval
            interval) {
        SjukfallCertificateWorkCapacity wc = new SjukfallCertificateWorkCapacity();
        wc.setCapacityPercentage(workCapacity);
        wc.setFromDate(PartialConverter.partialToString(new Partial(interval.fromAsLocalDate())));
        wc.setToDate(PartialConverter.partialToString(new Partial(interval.tomAsLocalDate())));
        return wc;
    }
}
