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

package se.inera.intyg.intygstjanst.web.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import se.inera.intyg.intygstjanst.web.service.dto.DiagnosisFromFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class IcdCodeConverter {

    private static final String CODE_HEADING = "Kod";
    private static final int DIAGNOSIS_CODE_INDEX = 0;
    private static final int DIAGNOSIS_TITLE_INDEX = 3;
    private final ResourceLoader resourceLoader;

    public Map<String, String> convert(String file) throws IOException {
        final var resource = resourceLoader.getResource(file);
        final var diagnosisMap = getStream(resource)
            .map(this::toDiagnosis)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(DiagnosisFromFile::getCode, DiagnosisFromFile::getName));

        log.info("Loaded {} codes from file {}", diagnosisMap.size(), file);
        return diagnosisMap;
    }

    private static Stream<String> getStream(Resource resource) throws IOException {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
            IOUtils.lineIterator(resource.getInputStream(), StandardCharsets.UTF_8), Spliterator.ORDERED), false);
    }

    private DiagnosisFromFile toDiagnosis(String line) {
        final var text = line.replace("\"", "").split("\t");
        if (isDiagnosisChapter(text) || isDiagnosisGroup(text) || isNotActive(text) || isHeading(text)) {
            return null;
        }
        return new DiagnosisFromFile(text[DIAGNOSIS_CODE_INDEX].replace(".", ""), text[DIAGNOSIS_TITLE_INDEX]);
    }

    private boolean isHeading(String[] text) {
        return text[0].equals(CODE_HEADING);
    }

    private boolean isNotActive(String[] line) {
        return line[1].isEmpty();
    }

    private boolean isDiagnosisGroup(String[] line) {
        return line[0].contains("-");
    }

    private boolean isDiagnosisChapter(String[] line) {
        return Character.isDigit(line[0].charAt(0));
    }
}
