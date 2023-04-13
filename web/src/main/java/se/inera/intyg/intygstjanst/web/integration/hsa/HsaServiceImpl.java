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
package se.inera.intyg.intygstjanst.web.integration.hsa;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;

/**
 * Interfaces with {@link HsaOrganizationsService} from hsa-integration.
 *
 * Created by eriklupander on 2016-02-02.
 */
@Service
public class HsaServiceImpl implements HsaService {

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Override
    @PrometheusTimeMethod
    public List<String> getHsaIdForUnderenheter(String careUnitHsaId) {
        return hsaOrganizationsService.getHsaIdForAktivaUnderenheter(careUnitHsaId);
    }

    @Override
    @PrometheusTimeMethod
    public String getHsaIdForVardgivare(String careUnitHsaId) {
        return hsaOrganizationsService.getVardgivareOfVardenhet(careUnitHsaId);
    }

    @Override
    public HsaResponse getHsaIdsForCareProviderAndSubUnits(String careUnitId) {
        final var careProviderId = hsaOrganizationsService.getVardgivareOfVardenhet(careUnitId);
        final var unitAndSubUnits = hsaOrganizationsService.getHsaIdForAktivaUnderenheter(careUnitId);
        unitAndSubUnits.add(careUnitId);
        return new HsaResponse(careProviderId, unitAndSubUnits);
    }
}
