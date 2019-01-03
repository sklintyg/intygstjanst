/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiver;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiverDao;
import se.inera.intyg.intygstjanst.web.service.ReceiverService;

@Service
public class ReceiverServiceImpl implements ReceiverService {

    @Autowired
    private ApprovedReceiverDao approvedReceiverDao;

    @Transactional
    @Override
    public void registerApprovedReceiver(ApprovedReceiver approvedReceiver) {
        approvedReceiverDao.store(approvedReceiver);
    }

    @Transactional
    @Override
    public void clearApprovedReceiversForCertificate(String intygsId) {
        approvedReceiverDao.clearApprovedReceiversForCertificate(intygsId);
    }
}
