/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Partial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(CertificateToSjukfallCertificateConverter.class);


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
                .signingDateTime(certificate.getSignedDate())
                .certificateType(certificate.getType())
                .deleted(certificate.getDeleted())
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

    public boolean isConvertableFk7263(Utlatande utlatande) {
        if (!(utlatande instanceof se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande)) {
            throw new IllegalArgumentException("Cannot validate " + utlatande.getClass().getName() + " to SjukfallCertificate, not of fk7263 type.");
        }

        se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande fkUtlatande = (se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande) utlatande;

        if (fkUtlatande.isAvstangningSmittskydd()) {
            LOG.debug("Not converting Intyg {0} to SjukfallCertificate, is smittskydd.");
            return false;
        }

        if (fkUtlatande.getDiagnosKod() == null || fkUtlatande.getDiagnosKod().trim().equals("")) {
            LOG.debug("Not converting Intyg {0} to SjukfallCertificate, has no diagnoseCode.");
            return false;
        }

        return true;
    }
}