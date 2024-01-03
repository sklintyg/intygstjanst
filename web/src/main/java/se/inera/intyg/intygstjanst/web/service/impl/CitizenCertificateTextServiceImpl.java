/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateTextService;

@Service
public class CitizenCertificateTextServiceImpl implements CitizenCertificateTextService {

    private final IntygModuleRegistry intygModuleRegistry;

    public CitizenCertificateTextServiceImpl(IntygModuleRegistry intygModuleRegistry) {
        this.intygModuleRegistry = intygModuleRegistry;
    }

    @Override
    public String getTypeName(String typeId) {
        try {
            return intygModuleRegistry.getModuleEntryPoint(typeId).getModuleName();
        } catch (ModuleNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getAdditionalInfoLabel(String typeId, String typeVersion) {
        try {
            return intygModuleRegistry.getModuleApi(typeId, typeVersion).getAdditionalInfoLabel();
        } catch (ModuleNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
