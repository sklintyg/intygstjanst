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

import jakarta.xml.ws.WebServiceException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.services.HsatkEmployeeService;
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

    @Autowired
    private HsatkEmployeeService hsaEmployeeService;

    private static final String EMPLOYEE_NAME_CACHE = "employeeNameCache";


    private static final Logger LOG = LoggerFactory.getLogger(HsaServiceImpl.class);

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
    public List<String> getHsaIdsForCareUnitAndSubUnits(String careUnitId) {
        final var unitAndSubUnits = hsaOrganizationsService.getHsaIdForAktivaUnderenheter(careUnitId);
        return Stream.concat(Stream.of(careUnitId), unitAndSubUnits.stream())
            .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = EMPLOYEE_NAME_CACHE, key = "#doctorId")
    public String getHsaEmployeeName(String doctorId) {
        try {
            final var employee = hsaEmployeeService.getEmployee(null, doctorId, null);
            if (employee == null || employee.isEmpty()) {
                return doctorId;
            }
            return getName(employee);
        } catch (WebServiceException e) {
            LOG.error(e.getMessage());
            throw new WebServiceException();
        }
    }

    private String getName(List<PersonInformation> employeeInfo) {
        return employeeInfo.get(0).getGivenName() + " " + employeeInfo.get(0).getMiddleAndSurName();
    }
}
