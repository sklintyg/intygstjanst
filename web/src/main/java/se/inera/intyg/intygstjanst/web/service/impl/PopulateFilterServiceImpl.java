/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.service.impl;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.HsaServiceProvider;
import se.inera.intyg.intygstjanst.web.service.PopulateFilterService;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersRequestDTO;

@Service
public class PopulateFilterServiceImpl implements PopulateFilterService {

    private static final Logger LOG = LoggerFactory.getLogger(PopulateFilterServiceImpl.class);
    private final SjukfallCertificateDao sjukfallCertificateDao;
    private final HsaServiceProvider hsaServiceProvider;

    public PopulateFilterServiceImpl(SjukfallCertificateDao sjukfallCertificateDao, HsaServiceProvider hsaServiceProvider) {
        this.sjukfallCertificateDao = sjukfallCertificateDao;
        this.hsaServiceProvider = hsaServiceProvider;
    }

    @Override
    public List<SjukfallCertificate> getActiveSickLeaveCertificates(PopulateFiltersRequestDTO populateFiltersRequestDTO) {
        final var careUnitId = populateFiltersRequestDTO.getCareUnitId();
        final var maxDaysSinceSickLeaveCompleted = populateFiltersRequestDTO.getMaxDaysSinceSickLeaveCompleted();

        if (careUnitId == null || careUnitId.isEmpty()) {
            LOG.debug("No care unit id was provided, returning empty list.");
            return Collections.emptyList();
        }
        final var careGiverHsaId = hsaServiceProvider.getCareGiverHsaId(careUnitId);
        final var unitAndRelatedSubUnits = hsaServiceProvider.getUnitAndRelatedSubUnits(careUnitId);

        LOG.debug("Getting doctors with active sick leaves for care unit:  {}", careUnitId);

        return sjukfallCertificateDao.findDoctorsWithActiveSickLeavesForCareUnits(careGiverHsaId,
            unitAndRelatedSubUnits, maxDaysSinceSickLeaveCompleted);
    }
}
