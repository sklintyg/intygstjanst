/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.application.citizen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateTextServiceTest {

  private static final String TYPE_NAME = "TYPE_NAME";
  private static final String ADDITIONAL_INFO_LABEL = "Additional info label";

  @Mock IntygModuleRegistry intygModuleRegistry;

  @InjectMocks CitizenCertificateTextService citizenCertificateTextService;

  @Nested
  class TypeName {

    private final ModuleEntryPoint entryPoint = mock(ModuleEntryPoint.class);

    @BeforeEach
    void setup() throws ModuleNotFoundException {
      Mockito.when(intygModuleRegistry.getModuleEntryPoint(any())).thenReturn(entryPoint);
      Mockito.when(entryPoint.getModuleName()).thenReturn(TYPE_NAME);
    }

    @Test
    void shouldReturnValue() throws ModuleNotFoundException {
      final var result = citizenCertificateTextService.getTypeName("id");

      assertEquals(TYPE_NAME, result);
    }
  }

  @Nested
  class AdditionalInfoLabel {

    private final ModuleApi moduleApi = mock(ModuleApi.class);

    @BeforeEach
    void setup() throws ModuleNotFoundException {
      Mockito.when(intygModuleRegistry.getModuleApi(anyString(), anyString()))
          .thenReturn(moduleApi);
      Mockito.when(moduleApi.getAdditionalInfoLabel()).thenReturn(ADDITIONAL_INFO_LABEL);
    }

    @Test
    void shouldReturnValue() throws ModuleNotFoundException {
      final var result = citizenCertificateTextService.getAdditionalInfoLabel("id", "version");

      assertEquals(ADDITIONAL_INFO_LABEL, result);
    }
  }
}
