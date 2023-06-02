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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.service.DiagnosisDescriptionProvider;

@ExtendWith(MockitoExtension.class)
class DiagnosisDescriptionServiceImplTest {


    @Mock
    private DiagnosisDescriptionProvider diagnosisDescriptionProvider;
    @InjectMocks
    private DiagnosisDescriptionServiceImpl diagnosisDescriptionService;
    private static final String B45 = "B45";
    private static final String B45_EXPECTED_RESULT = "Kryptokockos";

    @BeforeEach
    void setUp() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);
        when(diagnosisDescriptionProvider.getDiagnosisDescriptionMap()).thenReturn(getDescriptions());

        Method postConstruct = DiagnosisDescriptionServiceImpl.class.getDeclaredMethod("init", null);
        postConstruct.setAccessible(true);
        postConstruct.invoke(diagnosisDescriptionService);
    }

    @Test
    void shouldReturnDescriptionForCode() {
        final var result = diagnosisDescriptionService.getDiagnosisDescriptionFromSickLeave(B45);
        assertEquals(B45_EXPECTED_RESULT, result);
    }


    private Map<String, String> getDescriptions() {
        final var descriptionMap = new HashMap<String, String>();
        descriptionMap.put("B45", "Kryptokockos");
        return descriptionMap;
    }
}
