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

import java.util.List;
import java.util.stream.Collectors;
import javax.xml.ws.WebServiceException;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaEmployeeServiceImpl;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.service.DecorateSickLeaveInformationService;

public class DecorateSickLeaveInformationServiceImpl implements DecorateSickLeaveInformationService {

    private final HsaEmployeeServiceImpl hsaEmployeeService;

    public DecorateSickLeaveInformationServiceImpl(HsaEmployeeServiceImpl hsaEmployeeService) {
        this.hsaEmployeeService = hsaEmployeeService;
    }

    @Override
    public void decorate(List<SjukfallEnhet> sickLeaves) {
        sickLeaves.forEach(this::updateEmployeeName);
        decorateWithHsaId(sickLeaves);
    }

    private void updateEmployeeName(SjukfallEnhet sickLeave) {
        if (sickLeave.getLakare() == null) {
            return;
        }
        final var employeeName = getHsaEmployee(sickLeave.getLakare().getId());
        setEmployeeNameIfFound(sickLeave, employeeName);
    }

    private static void setEmployeeNameIfFound(SjukfallEnhet sickLeave, String employeeHsaName) {
        if (employeeHsaName != null) {
            sickLeave.getLakare().setNamn(employeeHsaName);
        } else {
            sickLeave.getLakare().setNamn(sickLeave.getLakare().getId());
        }
    }

    private String getHsaEmployee(String doctorId) {
        try {
            final var employee = hsaEmployeeService.getEmployee(doctorId, null, null);
            if (employee == null || employee.isEmpty()) {
                return doctorId;
            }
            return getName(employee);
        } catch (WebServiceException e) {
            throw new WebServiceException();
        }
    }

    private String getName(List<PersonInformation> employeeInfo) {
        return employeeInfo.get(0).getGivenName() + " " + employeeInfo.get(0).getMiddleAndSurName();
    }

    public void decorateWithHsaId(List<SjukfallEnhet> sickLeaves) {
        long numberOfHsaIds = sickLeaves.stream().map(sickLeave -> sickLeave.getLakare().getId()).distinct().count();
        long numberOfLakareNames = sickLeaves.stream().map(sickLeave -> sickLeave.getLakare().getNamn()).distinct().count();
        if (numberOfHsaIds == numberOfLakareNames) {
            return;
        }
        sickLeaves.stream()
            .map(SjukfallEnhet::getLakare)
            .collect(Collectors.groupingBy(Lakare::getNamn))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().stream()
                .map(Lakare::getId)
                .distinct()
                .count() > 1)
            .forEach(entry -> entry.getValue().forEach(lakare ->
                lakare.setNamn(lakare.getNamn() + " (" + lakare.getId() + ")")
            ));
    }
}
