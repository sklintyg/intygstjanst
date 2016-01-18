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

package se.inera.intyg.intygstjanst.web.service;

import java.util.List;
import java.util.Set;

import se.inera.intyg.common.support.modules.support.api.dto.TransportModelVersion;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

public interface RecipientService {

    /**
     * Get the {@link Recipient} that corresponds to a given logical address.
     *
     * @param logicalAddress
     *            the logical address to check
     * @return {@link Recipient}
     * @throws RecipientUnknownException
     */
    Recipient getRecipientForLogicalAddress(String logicalAddress) throws RecipientUnknownException;

    /**
     * Get the {@link Recipient} that corresponds to a given id.
     *
     * @param recipientId
     *            the id to check
     * @return {@link Recipient}
     * @throws RecipientUnknownException
     */
    Recipient getRecipient(String recipientId) throws RecipientUnknownException;

    /**
     * List all {@link Recipient}[s] currently known.
     *
     * @return List of {@link Recipient}[s]
     */
    List<Recipient> listRecipients();

    /**
     * Get a list of registered recipients for a certain {@link CertificateType}.
     *
     * @return a List of {@link Recipient}
     */
    List<Recipient> listRecipients(CertificateType certificateType) throws RecipientUnknownException;

    /**
     * List the {@link CertificateType}[s] the specified {@link Recipient} accepts.
     *
     * @param recipient
     *            {@link Recipient}
     * @return a List of Strings representing the accepted types
     */
    Set<CertificateType> listCertificateTypes(Recipient recipient);

    /**
     * Get the {@link TransportModelVersion} for a specific logicalAddress and certificateType.
     *
     * @param logicalAddress String
     * @param certificateType String
     *
     * @return the accepted {@link TransportModelVersion}
     */
    TransportModelVersion getVersion(String logicalAddress, String certificateType) throws RecipientUnknownException;

}
