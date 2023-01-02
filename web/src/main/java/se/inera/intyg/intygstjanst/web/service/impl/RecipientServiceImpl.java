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

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiver;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiverDao;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.repo.RecipientRepo;

@Service
public class RecipientServiceImpl implements RecipientService {

    @Autowired
    private ApprovedReceiverDao approvedReceiverDao;

    @Autowired
    private RecipientRepo recipientRepo;

    @Override
    public Recipient getRecipientForLogicalAddress(String logicalAddress) throws RecipientUnknownException {
        return recipientRepo.getRecipientForLogicalAddress(logicalAddress);
    }

    @Override
    public Recipient getRecipient(String recipientId) throws RecipientUnknownException {
        return recipientRepo.getRecipient(recipientId);
    }

    @Override
    public List<Recipient> listRecipients() {
        return recipientRepo.listRecipients();
    }

    @Override
    public List<Recipient> listRecipients(String certificateId) {
        // Get list of registered possible receivers.
        List<ApprovedReceiver> possibleReceivers =
            approvedReceiverDao.getApprovedReceiverIdsForCertificate(certificateId);

        // Reduce to approved recipients
        return recipientRepo.listRecipients().stream()
            .filter(isApprovedRecipient(possibleReceivers))
            .collect(Collectors.toList());
    }

    @Override
    public List<Recipient> listRecipients(CertificateType certificateType) {
        // Filter out HSVARD and INVANA recipients, as these are in fact Webcert/intygstjansten and Mina intyg)
        return recipientRepo.listRecipients().stream()
            .filter(r -> !getPrimaryRecipientHsvard().getId().equals(r.getId())
                && !getPrimaryRecipientInvana().getId().equals(r.getId()))
            .filter(r -> r.getCertificateTypes().contains(certificateType.getCertificateTypeId()))
            .collect(Collectors.toList());
    }

    @Override
    public Recipient getPrimaryRecipientFkassa() {
        return recipientRepo.getRecipientFkassa();
    }

    @Override
    public Recipient getPrimaryRecipientHsvard() {
        return recipientRepo.getRecipientHsvard();
    }

    @Override
    public Recipient getPrimaryRecipientInvana() {
        return recipientRepo.getRecipientInvana();
    }

    @Override
    public Recipient getPrimaryRecipientTransp() {
        return recipientRepo.getRecipientTransp();
    }

    private Predicate<Recipient> isApprovedRecipient(List<ApprovedReceiver> receivers) {
        return recipient -> !getPrimaryRecipientHsvard().getId().equals(recipient.getId())
            && !getPrimaryRecipientInvana().getId().equals(recipient.getId())
            && receivers.stream()
            .anyMatch(approvedReceiver -> approvedReceiver.isApproved()
                && approvedReceiver.getReceiverId().equals(recipient.getId())
            );
    }


}
