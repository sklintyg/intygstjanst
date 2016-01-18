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
