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
package se.inera.intyg.intygstjanst.web.integration.validator;

import static java.util.Objects.nonNull;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.intyg.infra.integration.pu.services.validator.PUResponseValidator;
import se.riv.strategicresourcemanagement.persons.person.v3.PersonRecordType;
import se.riv.strategicresourcemanagement.persons.person.v3.RequestedPersonRecordType;

/**
 * Intygstjanst implementation always allows testIndicated persons to be returned from PU.
 */
public class IntygstjanstPUResponseValidator implements PUResponseValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public IntygstjanstPUResponseValidator() {
        LOG.info("**** IntygstjanstPUResponseValidator activated - PU PersonRecords with testIndicator set will be allowed ****");
    }

    @Override
    public boolean isFoundAndCorrectStatus(RequestedPersonRecordType requestedPersonRecordType) {
        final boolean found = nonNull(requestedPersonRecordType) && nonNull(requestedPersonRecordType.getPersonRecord());

        if (found) {
            if (isPersonTestIndicated(requestedPersonRecordType.getPersonRecord())) {
                LOG.debug("Fetched person IS a test-indicated person");
            } else {
                LOG.debug("Fetched person is NOT a test-indicated person");
            }
        }

        return found;
    }

    private boolean isPersonTestIndicated(PersonRecordType person) {
        return BooleanUtils.toBoolean(person.isTestIndicator());
    }
}
