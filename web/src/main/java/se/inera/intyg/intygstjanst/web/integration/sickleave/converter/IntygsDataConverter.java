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

package se.inera.intyg.intygstjanst.web.integration.sickleave.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.Formaga;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.integration.rehabstod.converter.SjukfallCertificateIntygsDataConverter;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsData;

@Component
public class IntygsDataConverter {

    public List<IntygData> convert(List<SjukfallCertificate> sjukfallCertificateList) {
        return new ArrayList<>(
            new SjukfallCertificateIntygsDataConverter().buildIntygsData(sjukfallCertificateList)).stream()
            .map((this::map)).collect(Collectors.toList());
    }

    public IntygData map(IntygsData from) {
        IntygData to = new IntygData();

        try {
            to.setIntygId(from.getIntygsId());
            to.setPatientId(from.getPatient().getPersonId().getExtension());
            to.setPatientNamn(from.getPatient().getFullstandigtNamn());
            to.setLakareId(from.getSkapadAv().getPersonalId().getExtension());
            to.setLakareNamn(from.getSkapadAv().getFullstandigtNamn());
            to.setVardenhetId(from.getSkapadAv().getEnhet().getEnhetsId().getExtension());
            to.setVardenhetNamn(from.getSkapadAv().getEnhet().getEnhetsnamn());
            to.setVardgivareId(from.getSkapadAv().getEnhet().getVardgivare().getVardgivarId().getExtension());
            to.setVardgivareNamn(from.getSkapadAv().getEnhet().getVardgivare().getVardgivarnamn());
            to.setDiagnosKod(DiagnosKod.create(from.getDiagnoskod()));
            to.setFormagor(mapFormagor(from.getArbetsformaga().getFormaga()));
            to.setSigneringsTidpunkt(from.getSigneringsTidpunkt());
            to.setEnkeltIntyg(from.isEnkeltIntyg());
            to.setBiDiagnoser(mapDiagnoser(from.getBidiagnoser()));
            to.setSysselsattning(from.getSysselsattning());

        } catch (Exception e) {
            throw new RuntimeException("Error mapping Intygstjänsten's format to SjukfallEngine format", e);
        }

        return to;
    }

    private List<DiagnosKod> mapDiagnoser(List<String> from) {
        return Optional.ofNullable(from).orElse(Collections.emptyList()).stream()
            .map(DiagnosKod::create)
            .collect(Collectors.toList());
    }

    private List<Formaga> mapFormagor(List<se.riv.clinicalprocess.healthcond.rehabilitation.v1.Formaga> from) {
        return Optional.ofNullable(from).orElse(Collections.emptyList()).stream()
            .map(this::createFormaga)
            .collect(Collectors.toList());
    }

    private Formaga createFormaga(se.riv.clinicalprocess.healthcond.rehabilitation.v1.Formaga from) {
        return new Formaga(from.getStartdatum(), from.getSlutdatum(), from.getNedsattning());
    }
}
