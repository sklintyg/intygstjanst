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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterProvider;

@Component
public class DiagnosisChapterProviderImpl implements DiagnosisChapterProvider {

    @Value("${it.diagnosis.chapters.file}")
    private String diagnosisChaptersFile;
    private final ResourceLoader resourceLoader;

    public DiagnosisChapterProviderImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public List<DiagnosKapitel> getDiagnosisChapters() throws IOException {
        Resource resource = resourceLoader.getResource(diagnosisChaptersFile);

        List<DiagnosKapitel> list = new ArrayList<>();
        try (LineIterator it = IOUtils.lineIterator(resource.getInputStream(), "UTF-8")) {

            while (it.hasNext()) {
                final String line = it.nextLine();
                list.add(new DiagnosKapitel(line));
            }
        }
        return list;
    }
}
