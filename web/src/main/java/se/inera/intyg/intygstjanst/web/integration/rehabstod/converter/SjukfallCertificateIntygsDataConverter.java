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
package se.inera.intyg.intygstjanst.web.integration.rehabstod.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;

import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Arbetsformaga;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Diagnos;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Enhet;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Formaga;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsData;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Patient;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Vardgivare;

/**
 * Created by eriklupander on 2016-02-04.
 */
public class SjukfallCertificateIntygsDataConverter {

    public List<IntygsData> buildIntygsData(List<SjukfallCertificate> sjukfallCertificates) {
        List<IntygsData> intygsDataList = new ArrayList<>();

        for (SjukfallCertificate sc : sjukfallCertificates) {
            IntygsData intygsData = new IntygsData();

            IntygId intygId = new IntygId();
            intygId.setExtension(sc.getId());
            intygsData.setIntygsId(intygId);

            intygsData.setPatient(buildPatient(sc.getCivicRegistrationNumber(), sc.getPatientFirstName(), sc.getPatientLastName()));
            intygsData.setDiagnos(buildDiagnos("TODO", sc.getDiagnoseCode()));

            Vardgivare vardgivare = buildVardgivare(sc.getCareGiverId());
            Enhet enhet = buildEnhet(sc.getCareUnitId(), sc.getCareUnitName(), vardgivare);
            intygsData.setEnhet(enhet);

            intygsData.setSkapadAv(buildHoSPerson(intygsData.getEnhet(), sc.getSigningDoctorName(), sc.getSigningDoctorId()));
            intygsData.setEnkeltIntyg(false);

            Arbetsformaga arbetsformaga = new Arbetsformaga();
            arbetsformaga.getFormaga().addAll(buildFormaga(sc.getSjukfallCertificateWorkCapacity()));
            intygsData.setArbetsformaga(arbetsformaga);

            intygsDataList.add(intygsData);
        }

        return intygsDataList;

    }

    private Vardgivare buildVardgivare(String vardgivarId) {
        Vardgivare vardgivare = new Vardgivare();
        HsaId hsaId = new HsaId();
        hsaId.setExtension(vardgivarId);
        vardgivare.setVardgivareId(hsaId);
        return vardgivare;
    }

    private List<Formaga> buildFormaga(List<SjukfallCertificateWorkCapacity> workCapacities) {

        return workCapacities.stream().map(wc -> {
            Formaga formaga = new Formaga();
            formaga.setNedsattning(wc.getCapacityPercentage());
            formaga.setStartdatum(LocalDate.parse(wc.getFromDate()));
            formaga.setSlutdatum(LocalDate.parse(wc.getToDate()));
            return formaga;
        }).collect(Collectors.toList());
    }

    private Diagnos buildDiagnos(String grupp, String kod) {
        Diagnos diagnos = new Diagnos();
        diagnos.setGrupp(grupp);
        diagnos.setKod(kod);

        return diagnos;
    }

    private Patient buildPatient(String pnr, String fornamn, String efternamn) {
        Patient patient = new Patient();
        PersonId personId = new PersonId();
        personId.setExtension(pnr);
        patient.setPersonId(personId);
        patient.setFornamn(fornamn);
        patient.setEfternamn(efternamn);
        return patient;
    }

    private Enhet buildEnhet(String hsaId, String hsaName, Vardgivare vardgivare) {
        Enhet enhet = new Enhet();
        HsaId hsaIdType = new HsaId();
        hsaIdType.setExtension(hsaId);
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
        hsaIdType.setExtension(hsaId);
        hosPerson.setPersonalId(hsaIdType);
        return hosPerson;
    }

}
