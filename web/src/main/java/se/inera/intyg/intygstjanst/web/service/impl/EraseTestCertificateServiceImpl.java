/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiverDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.EraseTestCertificateService;

@Service
public class EraseTestCertificateServiceImpl implements EraseTestCertificateService {
    @Autowired
    private CertificateDao certificateDao;

    @Autowired
    private RelationDao relationDao;

    @Autowired
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Autowired
    private ApprovedReceiverDao approvedReceiverDao;

    @Override
    @Transactional
    public void eraseTestCertificates(List<String> testCertificateIds) {
        certificateDao.eraseTestCertificates(testCertificateIds);
        relationDao.eraseTestCertificates(testCertificateIds);
        approvedReceiverDao.eraseTestCertificates(testCertificateIds);
        sjukfallCertificateDao.eraseTestCertificates(testCertificateIds);
    }
}
