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
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.DiagnosisDescriptionProvider;
import se.inera.intyg.intygstjanst.web.service.DiagnosisDescriptionService;

@Service
public class DiagnosisDescriptionServiceImpl implements DiagnosisDescriptionService {

    @Autowired
    private DiagnosisDescriptionProvider diagnosisDescriptionProvider;
    private static final Logger LOG = LoggerFactory.getLogger(DiagnosisDescriptionServiceImpl.class);
    private Map<String, String> diagnosisDescriptionsMap;

    @PostConstruct
    private void init() {
        try {
            diagnosisDescriptionsMap = diagnosisDescriptionProvider.getDiagnosisDescription();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load diagnosis chapters!", e);
        }
        LOG.info("Loaded " + diagnosisDescriptionsMap.size() + " diagnosis chapter definitions");
    }

    @Override
    public String getDiagnosisDescriptionFromSickLeave(String diagnosisCode) {
        return diagnosisDescriptionsMap.get(diagnosisCode);
    }
}
