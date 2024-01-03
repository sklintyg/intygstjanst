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

import java.io.IOException;
import java.nio.charset.Charset;
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

    @Value("${it.diagnosisCodes.icd10se.file1}")
    private String diagnoseCodeIcd10SeFile1;

    @Value("${it.diagnosisCodes.icd10se.file2}")
    private String diagnoseCodeIcd10SeFile2;

    @Value("${it.diagnosisCodes.icd10se.file3}")
    private String diagnoseCodeIcd10SeFile3;

    @Value("${it.diagnosisCodes.ksh97p_kod.file}")
    private String diagnosKodKS97PKodFile;
    private final ResourceLoader resourceLoader;

    public DiagnosisDescriptionProviderImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Map<String, String> getDiagnosisDescription() throws IOException {
        final var diagnosisDescriptionMap = new HashMap<String, String>();
        diagnosisDescriptionMap.putAll(loadDiagnosFile(diagnoseCodeIcd10SeFile1, StandardCharsets.UTF_8));
        diagnosisDescriptionMap.putAll(loadDiagnosFile(diagnoseCodeIcd10SeFile2, StandardCharsets.UTF_8));
        diagnosisDescriptionMap.putAll(loadDiagnosFile(diagnoseCodeIcd10SeFile3, StandardCharsets.UTF_8));
        diagnosisDescriptionMap.putAll(loadDiagnosFile(diagnosKodKS97PKodFile, StandardCharsets.ISO_8859_1));
        return diagnosisDescriptionMap;
    }

    private Map<String, String> loadDiagnosFile(final String file, Charset fileEncoding) throws IOException {
        final var resource = resourceLoader.getResource(file);
        final var diagnosisDescriptionMap = new HashMap<String, String>();
        try (LineIterator it = IOUtils.lineIterator(resource.getInputStream(), fileEncoding)) {
            while (it.hasNext()) {
                final String line = it.nextLine();
                final var diagnosisCode = new DiagnosisFromFile(line, diagnosisDescriptionMap.size() == 0);
                if (diagnosisCode.getCode() != null) {
                    diagnosisDescriptionMap.put(diagnosisCode.getCode(), diagnosisCode.getName());
                }
            }
        }

        return diagnosisDescriptionMap;
    }

}
