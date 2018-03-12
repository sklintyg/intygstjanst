/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.fkparent.model.internal.Diagnos;
import se.inera.intyg.common.lisjp.model.internal.LisjpUtlatande;
import se.inera.intyg.common.lisjp.model.internal.Sjukskrivning;
import se.inera.intyg.common.support.model.InternalLocalDateInterval;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.intygstjanst.persistence.model.builder.SjukfallCertificateBuilder;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    // Ur kodverk KV_FKMU_0002
    private static final String NUVARANDE_ARBETE = "NUVARANDE_ARBETE";
    private static final String ARBETSSOKANDE = "ARBETSSOKANDE";
    private static final String FORALDRALEDIG = "FORALDRALEDIG";

    /**
     * Converts a fk7263 certificate into a SjukfallCertificate.
     *
     * @param certificate
     *            fk7263 cert
     * @param utlatande
     *            fk7263 Utlatande (will perform instanceof check internally)
     * @return
     *         A SjukfallCertificate
     * @throws
     *             IllegalArgumentException
     *             if fk7263 check fails.
     */
    public SjukfallCertificate convertFk7263(Certificate certificate, Utlatande utlatande) {

        if (!(utlatande instanceof Fk7263Utlatande)) {
            throw new IllegalArgumentException("Cannot convert " + utlatande.getClass().getName() + " to SjukfallCertificate");
        }

        Fk7263Utlatande fkUtlatande = (Fk7263Utlatande) utlatande;

        return new SjukfallCertificateBuilder(Strings.nullToEmpty(certificate.getId()).trim())
                .careGiverId(Strings.nullToEmpty(certificate.getCareGiverId()).trim())
                .careUnitId(Strings.nullToEmpty(certificate.getCareUnitId()).trim())
                .careUnitName(certificate.getCareUnitName())
                .certificateType(certificate.getType())
                .civicRegistrationNumber(Strings.nullToEmpty(certificate.getCivicRegistrationNumber().getPersonnummerWithDash()).trim())
                .signingDoctorName(certificate.getSigningDoctorName())
                .patientName(getPatientName(fkUtlatande.getGrundData().getPatient()))
                .diagnoseCode(fkUtlatande.getDiagnosKod())
                .signingDoctorId(Strings.nullToEmpty(fkUtlatande.getGrundData().getSkapadAv().getPersonId()).trim())
                .signingDoctorName(fkUtlatande.getGrundData().getSkapadAv().getFullstandigtNamn())
                .signingDateTime(certificate.getSignedDate())
                .certificateType(certificate.getType())
                .deleted(certificate.isRevoked())
                .workCapacities(buildWorkCapacitiesFk7263(fkUtlatande))
                .employment(buildFk7263Sysselsattning(fkUtlatande))
                .build();
    }

    // NUVARANDE_ARBETE|ARBETSSOKANDE|FORALDRALEDIG|STUDIER
    private String buildFk7263Sysselsattning(Fk7263Utlatande fkUtlatande) {
        List<String> sysselsattningar = new ArrayList<>();
        if (fkUtlatande.isNuvarandeArbete()) {
            sysselsattningar.add(NUVARANDE_ARBETE);
        }
        if (fkUtlatande.isArbetsloshet()) {
            sysselsattningar.add(ARBETSSOKANDE);
        }
        if (fkUtlatande.isForaldrarledighet()) {
            sysselsattningar.add(FORALDRALEDIG);
        }
        return sysselsattningar.stream().collect(Collectors.joining(","));
    }

    /**
     * Converts a fk7263 certificate into a SjukfallCertificate.
     *
     * @param certificate
     *            fk7263 cert
     * @param utlatande
     *            fk7263 Utlatande (will perform instanceof check internally)
     * @return
     *         A SjukfallCertificate
     * @throws
     *             IllegalArgumentException
     *             if fk7263 check fails.
     */
    public SjukfallCertificate convertLisjp(Certificate certificate, Utlatande utlatande) {

        if (!(utlatande instanceof LisjpUtlatande)) {
            throw new IllegalArgumentException("Cannot convert " + utlatande.getClass().getName() + " to SjukfallCertificate");
        }

        LisjpUtlatande lisjpUtlatande = (LisjpUtlatande) utlatande;

        SjukfallCertificate sc = new SjukfallCertificateBuilder(Strings.nullToEmpty(certificate.getId()).trim())
                .careGiverId(Strings.nullToEmpty(certificate.getCareGiverId()).trim())
                .careUnitId(Strings.nullToEmpty(certificate.getCareUnitId()).trim())
                .careUnitName(certificate.getCareUnitName())
                .certificateType(certificate.getType())
                .civicRegistrationNumber(Strings.nullToEmpty(certificate.getCivicRegistrationNumber().getPersonnummerWithDash()).trim())
                .signingDoctorName(certificate.getSigningDoctorName())
                .patientName(getPatientName(lisjpUtlatande.getGrundData().getPatient()))
                .diagnoseCode(lisjpUtlatande.getDiagnoser().get(0).getDiagnosKod())
                .signingDoctorId(Strings.nullToEmpty(lisjpUtlatande.getGrundData().getSkapadAv().getPersonId()).trim())
                .signingDoctorName(lisjpUtlatande.getGrundData().getSkapadAv().getFullstandigtNamn())
                .signingDateTime(certificate.getSignedDate())
                .certificateType(certificate.getType())
                .deleted(certificate.isRevoked())
                .workCapacities(buildWorkCapacitiesLisjp(lisjpUtlatande))
                .employment(lisjpUtlatande.getSysselsattning() != null ? lisjpUtlatande.getSysselsattning()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(s -> s.getTyp().getId())
                        .collect(Collectors.joining(",")) : null)
                .build();

        if (lisjpUtlatande.getDiagnoser().size() > 1) {
            for (int a = 1; a < lisjpUtlatande.getDiagnoser().size(); a++) {
                if (a == 1) {
                    sc.setBiDiagnoseCode1(lisjpUtlatande.getDiagnoser().get(a).getDiagnosKod());
                }
                if (a == 2) {
                    sc.setBiDiagnoseCode2(lisjpUtlatande.getDiagnoser().get(a).getDiagnosKod());
                }
            }
        }

        return sc;
    }

    private List<SjukfallCertificateWorkCapacity> buildWorkCapacitiesLisjp(LisjpUtlatande lisjpUtlatande) {
        List<SjukfallCertificateWorkCapacity> workCapacities = new ArrayList<>();
        lisjpUtlatande.getSjukskrivningar().stream().forEach(sjukskrivning -> {
            if (sjukskrivning.getSjukskrivningsgrad() == Sjukskrivning.SjukskrivningsGrad.HELT_NEDSATT) {
                workCapacities.add(buildWorkCapacity(WORK_CAPACITY_100, sjukskrivning.getPeriod()));
            }
            if (sjukskrivning.getSjukskrivningsgrad() == Sjukskrivning.SjukskrivningsGrad.NEDSATT_3_4) {
                workCapacities.add(buildWorkCapacity(WORK_CAPACITY_75, sjukskrivning.getPeriod()));
            }
            if (sjukskrivning.getSjukskrivningsgrad() == Sjukskrivning.SjukskrivningsGrad.NEDSATT_HALFTEN) {
                workCapacities.add(buildWorkCapacity(WORK_CAPACITY_50, sjukskrivning.getPeriod()));
            }
            if (sjukskrivning.getSjukskrivningsgrad() == Sjukskrivning.SjukskrivningsGrad.NEDSATT_1_4) {
                workCapacities.add(buildWorkCapacity(WORK_CAPACITY_25, sjukskrivning.getPeriod()));
            }
        });
        return workCapacities;
    }

    private List<SjukfallCertificateWorkCapacity> buildWorkCapacitiesFk7263(Fk7263Utlatande fkUtlatande) {
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

    private String getPatientName(Patient patient) {
        String name = "";

        if (patient == null) {
            return name;
        }

        String fnamn = patient.getFornamn();
        String mnamn = patient.getMellannamn();
        String enamn = patient.getEfternamn();

        if (fnamn != null) {
            name = fnamn.isEmpty() ? "" : fnamn;
        }

        if (mnamn != null) {
            name = mnamn.isEmpty() ? "" : name + " " + mnamn;
        }

        if (enamn != null) {
            name = enamn.isEmpty() ? "" : name + " " + enamn;
        }

        return name.trim();
    }

    private SjukfallCertificateWorkCapacity buildWorkCapacity(Integer workCapacity, InternalLocalDateInterval interval) {
        SjukfallCertificateWorkCapacity wc = new SjukfallCertificateWorkCapacity();
        wc.setCapacityPercentage(workCapacity);
        wc.setFromDate(interval.fromAsLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        wc.setToDate(interval.tomAsLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        return wc;
    }

    public boolean isConvertableFk7263(Utlatande utlatande) {
        if (utlatande == null || !(utlatande instanceof Fk7263Utlatande)) {
            return false;
        }

        Fk7263Utlatande fkUtlatande = (Fk7263Utlatande) utlatande;

        if (fkUtlatande.isAvstangningSmittskydd()) {
            LOG.debug("Intyg {} is not a valid SjukfallCertificate, is smittskydd.", fkUtlatande.getId());
            return false;
        }

        if (fkUtlatande.getDiagnosKod() == null || "".equals(fkUtlatande.getDiagnosKod().trim())) {
            LOG.debug("Intyg {} is not a valid SjukfallCertificate, has no diagnoseCode.", fkUtlatande.getId());
            return false;
        }

        return true;
    }

    public boolean isConvertableLisjp(Utlatande utlatande) {

        if (utlatande == null || !(utlatande instanceof LisjpUtlatande)) {
            return false;
        }

        LisjpUtlatande lisjpUtlatande = (LisjpUtlatande) utlatande;
        if (lisjpUtlatande.getAvstangningSmittskydd() != null && lisjpUtlatande.getAvstangningSmittskydd()) {
            LOG.debug("Intyg {} is not a valid SjukfallCertificate, is smittskydd.", lisjpUtlatande.getId());
            return false;
        }

        Diagnos diagnos = lisjpUtlatande.getDiagnoser().stream().findFirst().orElse(null);
        if (diagnos == null || diagnos.getDiagnosKod() == null || "".equals(diagnos.getDiagnosKod().trim())) {
            LOG.debug("Intyg {} is not a valid SjukfallCertificate, has no diagnoseCode.", lisjpUtlatande.getId());
            return false;
        }

        return true;
    }
}
