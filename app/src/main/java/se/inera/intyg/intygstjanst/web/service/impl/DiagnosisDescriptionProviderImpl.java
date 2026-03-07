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
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import se.inera.intyg.intygstjanst.web.service.DiagnosisDescriptionProvider;
import se.inera.intyg.intygstjanst.web.service.dto.DiagnosisFromFile;

@Component
public class DiagnosisDescriptionProviderImpl implements DiagnosisDescriptionProvider {

    @Value("${it.diagnosisCodes.icd10se.file}")
    private String diagnoseCodeIcd10SeFile;
    @Value("${it.diagnosisCodes.ksh97p_kod.file}")
    private String diagnosKodKS97PKodFile;
    private final ResourceLoader resourceLoader;
    private final IcdCodeConverter icdCodeConverter;

    public DiagnosisDescriptionProviderImpl(IcdCodeConverter icdCodeConverter, ResourceLoader resourceLoader) {
        this.icdCodeConverter = icdCodeConverter;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Map<String, String> getDiagnosisDescription() throws IOException {
        final var diagnosisDescriptionMap = new HashMap<String, String>();
        diagnosisDescriptionMap.putAll(icdCodeConverter.convert(diagnoseCodeIcd10SeFile));
        diagnosisDescriptionMap.putAll(loadDiagnosFile(diagnosKodKS97PKodFile));
        return diagnosisDescriptionMap;
    }

    private Map<String, String> loadDiagnosFile(final String file) throws IOException {
        final var resource = resourceLoader.getResource(file);
        final var diagnosisDescriptionMap = new HashMap<String, String>();
        try (LineIterator it = IOUtils.lineIterator(resource.getInputStream(), StandardCharsets.ISO_8859_1)) {
            while (it.hasNext()) {
                final String line = it.next();
                final var diagnosisCode = new DiagnosisFromFile(line, diagnosisDescriptionMap.isEmpty());
                if (diagnosisCode.getCode() != null) {
                    diagnosisDescriptionMap.put(diagnosisCode.getCode(), diagnosisCode.getName());
                }
            }
        }

        return diagnosisDescriptionMap;
    }

}
