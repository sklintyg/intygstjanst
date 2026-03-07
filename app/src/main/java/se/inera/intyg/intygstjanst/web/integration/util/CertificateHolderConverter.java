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

package se.inera.intyg.intygstjanst.web.integration.util;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.dto.AdditionalMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateRelation;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Relation;

@Component
public class CertificateHolderConverter {

    public CertificateHolder convert(Intyg intyg, String type, String originalCertificate, String additionalInfo,
        AdditionalMetaData additionalMetaData) {

        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId(intyg.getIntygsId().getExtension());
        certificateHolder.setCareUnitId(intyg.getSkapadAv().getEnhet().getEnhetsId().getExtension());
        certificateHolder.setCareUnitName(intyg.getSkapadAv().getEnhet().getEnhetsnamn());
        certificateHolder.setCareGiverId(intyg.getSkapadAv().getEnhet().getVardgivare().getVardgivareId().getExtension());
        certificateHolder.setSigningDoctorId(intyg.getSkapadAv().getPersonalId().getExtension());
        certificateHolder.setSigningDoctorName(intyg.getSkapadAv().getFullstandigtNamn());
        certificateHolder.setCivicRegistrationNumber(createPnr(intyg));
        certificateHolder.setSignedDate(intyg.getSigneringstidpunkt());
        certificateHolder.setType(type);
        certificateHolder.setTypeVersion(intyg.getVersion());
        certificateHolder.setOriginalCertificate(originalCertificate);
        certificateHolder.setAdditionalInfo(additionalInfo);
        certificateHolder.setCertificateRelation(convertRelation(intyg.getIntygsId().getExtension(), intyg.getRelation()));
        certificateHolder.setAdditionalMetaData(additionalMetaData);
        return certificateHolder;
    }

    private CertificateRelation convertRelation(String intygsId, List<Relation> relations) {
        if (relations != null && relations.size() > 0) {
            return new CertificateRelation(intygsId, relations.get(0).getIntygsId().getExtension(),
                RelationKod.fromValue(relations.get(0).getTyp().getCode()), LocalDateTime.now());
        }
        return null;
    }

    private Personnummer createPnr(Intyg intyg) {
        String personId = null;
        try {
            personId = intyg.getPatient().getPersonId().getExtension();
        } catch (NullPointerException npe) {
            throw new RuntimeException("Could not get patient's personnummer from intyg");
        }

        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));

    }
}
