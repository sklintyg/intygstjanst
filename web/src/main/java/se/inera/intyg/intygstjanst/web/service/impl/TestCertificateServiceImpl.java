/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;
import se.inera.intyg.intygstjanst.web.integration.testcertificate.dto.TestCertificateEraseResult;
import se.inera.intyg.intygstjanst.web.service.EraseTestCertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.TestCertificateService;

/**
 * Implementation of {@link TestCertificateService}.
 */
@Service
public class TestCertificateServiceImpl implements TestCertificateService {

    private static final Logger LOG = LoggerFactory.getLogger(TestCertificateServiceImpl.class);

    @Autowired
    private CertificateDao certificateDao;

    @Autowired
    private RelationDao relationDao;

    @Autowired
    private EraseTestCertificateService eraseTestCertificateService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public TestCertificateEraseResult eraseTestCertificates(LocalDateTime from, LocalDateTime to) {
        final Set<String> erasedTestCertificates = new HashSet<>();
        final Set<String> failedTestCertificates = new HashSet<>();

        final List<Certificate> testCertificates = certificateDao.findTestCertificates(from, to);

        final Map<String, List<Relation>> testCertificatesWithRelations = getCertificateRelations(testCertificates);

        for (Certificate testCertificate: testCertificates) {
            if (skipIfAlreadyErasedDueToRelation(testCertificate.getId(), erasedTestCertificates)) {
                continue;
            }

            final List<String> idsToErase = getCertificateIdsToErase(testCertificate.getId(), testCertificatesWithRelations);

            final Map<String, String> unitMap = getUnitMapForLogging(idsToErase);

            final List<String> idsToLog = new ArrayList<>(idsToErase.size());

            try {
                eraseTestCertificateService.eraseTestCertificates(idsToErase);
                erasedTestCertificates.addAll(idsToErase);
                idsToLog.addAll(idsToErase);
            } catch (Exception ex) {
                LOG.error(
                    String.format("Couldn't not erase certificate with ids %s when erasing test certificates", idsToErase.toString()), ex);
                failedTestCertificates.addAll(idsToErase);
            }

            for (String idToLog: idsToLog) {
                monitoringLogService.logTestCertificateErased(idToLog, unitMap.get(idToLog));
            }
        }

        return TestCertificateEraseResult.create(erasedTestCertificates.size(), failedTestCertificates.size());
    }

    private boolean skipIfAlreadyErasedDueToRelation(String id, Set<String> erasedTestCertificates) {
        return erasedTestCertificates.contains(id);
    }

    private Map<String, List<Relation>> getCertificateRelations(List<Certificate> testCertificates) {
        final Map<String, List<Relation>> testCertificatesWithRelations = new HashMap<>(testCertificates.size());

        for (Certificate testCertificate : testCertificates) {
            if (isCertificatePartOfRelations(testCertificate.getId(), testCertificatesWithRelations)) {
                continue;
            }

            final List<Relation> relationIds = relationDao.getGraph(testCertificate.getId());
            if (relationIds != null && relationIds.size() > 0) {
                testCertificatesWithRelations.put(testCertificate.getId(), relationIds);
            }
        }

        return testCertificatesWithRelations;
    }

    private boolean isCertificatePartOfRelations(String id, Map<String, List<Relation>> testCertificatesWithRelations) {
        for (List<Relation> relationList: testCertificatesWithRelations.values()) {
            for (Relation relation: relationList) {
                if (id.equalsIgnoreCase(relation.getFromIntygsId()) || id.equalsIgnoreCase(relation.getToIntygsId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> getCertificateIdsToErase(String id, Map<String, List<Relation>> testCertificatesWithRelations) {
        final List<String> idsToErase = new ArrayList<>();
        idsToErase.add(id);
        if (testCertificatesWithRelations.containsKey(id)) {
            for (Relation relation: testCertificatesWithRelations.get(id)) {
                if (relation.getFromIntygsId() != null && !idsToErase.contains(relation.getFromIntygsId())) {
                    idsToErase.add(relation.getFromIntygsId());
                }
                if (relation.getToIntygsId() != null && !idsToErase.contains(relation.getToIntygsId())) {
                    idsToErase.add(relation.getToIntygsId());
                }
            }
        }
        return idsToErase;
    }

    private Map<String, String> getUnitMapForLogging(List<String> idsToErase) {
        final Map<String, String> unitMap = new HashMap<>();
        for (String idToErase: idsToErase) {
            try {
                final Certificate certificate = certificateDao.getCertificate(null, idToErase);
                unitMap.put(idToErase, certificate.getCareUnitId());
            } catch (Exception ex) {
                LOG.warn(String.format("Couldn't not retrieve certificate with id %s", idToErase), ex);
                unitMap.put(idToErase, "Could not fetch unit id");
            }
        }
        return unitMap;
    }
}
