/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration.rehabstod.converter;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Arbetsformaga;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Enhet;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Formaga;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsData;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Patient;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Vardgivare;

/**
 * Converts a list of {@link SjukfallCertificate} into a list of {@link IntygsData}.
 *
 * Created by eriklupander on 2016-02-04.
 */
public class SjukfallCertificateIntygsDataConverter {

    public List<IntygsData> buildIntygsData(List<SjukfallCertificate> sjukfallCertificates) {
        List<IntygsData> intygsDataList = new ArrayList<>();

        for (SjukfallCertificate sc : sjukfallCertificates) {
            IntygsData intygsData = new IntygsData();

            intygsData.setIntygsId(Strings.nullToEmpty(sc.getId()).trim());
            intygsData.setSigneringsTidpunkt(sc.getSigningDateTime());
            intygsData.setPatient(buildPatient(sc.getCivicRegistrationNumber(), sc.getPatientName()));
            intygsData.setDiagnoskod(sc.getDiagnoseCode());

            if (sc.getBiDiagnoseCode1() != null) {
                intygsData.getBidiagnoser().add(sc.getBiDiagnoseCode1());
            }
            if (sc.getBiDiagnoseCode2() != null) {
                intygsData.getBidiagnoser().add(sc.getBiDiagnoseCode2());
            }

            intygsData.getSysselsattning().addAll(buildSysselsattning(sc));

            Vardgivare vardgivare = buildVardgivare(sc.getCareGiverId());
            Enhet enhet = buildEnhet(sc.getCareUnitId(), sc.getCareUnitName(), vardgivare);

            intygsData.setSkapadAv(buildHoSPerson(enhet, sc.getSigningDoctorName(), sc.getSigningDoctorId()));
            intygsData.setEnkeltIntyg(false);

            Arbetsformaga arbetsformaga = new Arbetsformaga();
            arbetsformaga.getFormaga().addAll(buildFormaga(sc.getSjukfallCertificateWorkCapacity()));
            intygsData.setArbetsformaga(arbetsformaga);

            intygsDataList.add(intygsData);
        }

        return intygsDataList;

    }

    private List<String> buildSysselsattning(SjukfallCertificate sc) {
        List<String> sysselsattningList = new ArrayList<>();
        if (Strings.isNullOrEmpty(sc.getEmployment())) {
            return sysselsattningList;
        }

        sysselsattningList.addAll(Arrays.asList(sc.getEmployment().split(",", -1)));
        return sysselsattningList;
    }

    private Vardgivare buildVardgivare(String vardgivarId) {
        Vardgivare vardgivare = new Vardgivare();
        HsaId hsaId = new HsaId();
        hsaId.setExtension(Strings.nullToEmpty(vardgivarId).trim());
        vardgivare.setVardgivarId(hsaId);
        return vardgivare;
    }

    private List<Formaga> buildFormaga(List<SjukfallCertificateWorkCapacity> workCapacities) {

        return workCapacities.stream()
            .map(this::buildFormaga)
            .collect(Collectors.toList());
    }

    private Formaga buildFormaga(SjukfallCertificateWorkCapacity wc) {
        Formaga formaga = new Formaga();
        formaga.setNedsattning(wc.getCapacityPercentage());
        formaga.setStartdatum(LocalDate.parse(wc.getFromDate()));
        formaga.setSlutdatum(LocalDate.parse(wc.getToDate()));
        return formaga;
    }

    private Patient buildPatient(String pnr, String namn) {
        Patient patient = new Patient();
        PersonId personId = new PersonId();
        personId.setExtension(Strings.nullToEmpty(pnr).trim());
        patient.setPersonId(personId);
        patient.setFullstandigtNamn(namn);
        return patient;
    }

    private Enhet buildEnhet(String hsaId, String hsaName, Vardgivare vardgivare) {
        Enhet enhet = new Enhet();
        HsaId hsaIdType = new HsaId();
        hsaIdType.setExtension(Strings.nullToEmpty(hsaId).trim());
        enhet.setEnhetsId(hsaIdType);
        enhet.setEnhetsnamn(hsaName);
        enhet.setVardgivare(vardgivare);
        return enhet;
    }

    private HosPersonal buildHoSPerson(Enhet enhet, String namn, String hsaId) {
        HosPersonal hosPerson = new HosPersonal();
        hosPerson.setEnhet(enhet);
        hosPerson.setFullstandigtNamn(namn);
        HsaId hsaIdType = new HsaId();
        hsaIdType.setExtension(Strings.nullToEmpty(hsaId).trim());
        hosPerson.setPersonalId(hsaIdType);
        return hosPerson;
    }

}
