/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.Consent;
import se.inera.intyg.intygstjanst.persistence.model.dao.ConsentDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author andreaskaltenbach
 */
@Repository
public class SjukfallCertificateDaoImpl implements SjukfallCertificateDao {

    private static final Logger log = LoggerFactory.getLogger(SjukfallCertificateDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SjukfallCertificate> findActiveSjukfallCertificateForCareUnits(List<String> careUnitHsaIds) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        List<SjukfallCertificate> resultList = entityManager.createQuery("SELECT sc FROM SjukfallCertificate sc JOIN FETCH " +
                "sc.sjukfallCertificateWorkCapacity scwc WHERE " +
                "    sc.careUnitId IN (:careUnitHsaId) " +
                "AND scwc.fromDate <= :today " +
                "AND scwc.toDate >= :today " +
                "AND sc.deleted = false " +
                "ORDER BY sc.civicRegistrationNumber", SjukfallCertificate.class)
                .setParameter("careUnitHsaId", careUnitHsaIds.stream().collect(Collectors.joining(",")))
                .setParameter("today", today)
                .getResultList();

        // Remove this or change to debug later on.
        log.info("SjukfallCertificate query for {0} returned {1} items.", careUnitHsaIds, resultList.size());

        return resultList;
    }
}
