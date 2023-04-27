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

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKategori;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterProvider;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterService;

@Service
public class DiagnosisChapterServiceImpl implements DiagnosisChapterService {

    public static final DiagnosKapitel OGILTIGA_DIAGNOSKODER_KAPITEL = new DiagnosKapitel(
        new DiagnosKategori(' ', 0),
        new DiagnosKategori(' ', 0),
        "Utan giltig diagnoskod");
    private static final Logger LOG = LoggerFactory.getLogger(DiagnosisChapterServiceImpl.class);
    @Autowired
    private DiagnosisChapterProvider diagnosisChapterProvider;
    private List<DiagnosKapitel> diagnosisChapterList;

    @PostConstruct
    private void init() {
        try {
            diagnosisChapterList = diagnosisChapterProvider.getDiagnosisChapters();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load diagnosis chapters!", e);
        }
        LOG.info("Loaded " + diagnosisChapterList.size() + " diagnosis chapter definitions");
    }

    @Override
    public List<DiagnosKapitel> getDiagnosisChaptersFromSickLeaveCertificate(List<SjukfallCertificate> sickLeaveCertificates) {
        final var diagnosisForCareUnit = getDiagnosisForCareUnit(sickLeaveCertificates);
        return diagnosisForCareUnit.stream()
            .map((diagnosisCode) -> DiagnosKategori.extractFromString(diagnosisCode.getCleanedCode()))
            .map(this::getDiagnosisChapterForCategory)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public DiagnosKapitel getDiagnosisChaptersFromSickLeave(SjukfallEnhet sickLeave) {
        final var diagnosisCategory = DiagnosKategori.extractFromString(sickLeave.getDiagnosKod().getCleanedCode());
        return getDiagnosisChapterForCategory(diagnosisCategory);
    }

    @Override
    public DiagnosKapitel getDiagnosisChaptersFromIntygData(IntygData intygData) {
        final var diagnosisCategory = DiagnosKategori.extractFromString(intygData.getDiagnosKod().getCleanedCode());
        return getDiagnosisChapterForCategory(diagnosisCategory);
    }

    private List<DiagnosKod> getDiagnosisForCareUnit(List<SjukfallCertificate> sickLeaveCertificates) {
        return sickLeaveCertificates.stream()
            .map(SjukfallCertificate::getDiagnoseCode)
            .filter(Objects::nonNull)
            .distinct()
            .map(DiagnosKod::create)
            .collect(Collectors.toList());
    }

    @Override
    public DiagnosKapitel getDiagnosisChapter(DiagnosKod diagnosisCode) {
        if (diagnosisCode == null) {
            return OGILTIGA_DIAGNOSKODER_KAPITEL;
        }

        return getDiagnosisChapterForCategory(
            DiagnosKategori.extractFromString(diagnosisCode.getCleanedCode())
        );
    }

    @Override
    public DiagnosKapitel getDiagnosisChapterForCategory(Optional<DiagnosKategori> diagnosKategori) {
        return this.diagnosisChapterList.stream()
            .filter(diagnosisChapters -> diagnosisChapters.includes(diagnosKategori))
            .findFirst()
            .orElse(OGILTIGA_DIAGNOSKODER_KAPITEL);
    }
}
