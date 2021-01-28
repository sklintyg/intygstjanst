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
package se.inera.intyg.intygstjanst.web.service;

import java.util.List;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

public interface RecipientService {

    /**
     * Get the {@link Recipient} that corresponds to a given logical address.
     *
     * @param logicalAddress the logical address to check
     * @return {@link Recipient}
     */
    Recipient getRecipientForLogicalAddress(String logicalAddress) throws RecipientUnknownException;

    /**
     * Get the {@link Recipient} that corresponds to a given id.
     *
     * @param recipientId the id to check
     * @return {@link Recipient}
     */
    Recipient getRecipient(String recipientId) throws RecipientUnknownException;

    /**
     * List all {@link Recipient}[s] currently known.
     *
     * @return List of {@link Recipient}[s]
     */
    List<Recipient> listRecipients();

    /**
     * Get a list of registered recipients for a specific certificate}.
     *
     * @param certificateId the certificate id
     * @return a List of {@link Recipient}
     */
    List<Recipient> listRecipients(String certificateId);

    /**
     * Get a list of registered recipients for a certain {@link CertificateType}.
     *
     * @return a List of {@link Recipient}
     */
    List<Recipient> listRecipients(CertificateType certificateType);

    Recipient getPrimaryRecipientFkassa();

    Recipient getPrimaryRecipientHsvard();

    Recipient getPrimaryRecipientInvana();

    Recipient getPrimaryRecipientTransp();

}
