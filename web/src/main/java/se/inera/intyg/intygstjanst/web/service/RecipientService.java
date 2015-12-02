package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.common.support.modules.support.api.dto.TransportModelVersion;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

import java.util.List;
import java.util.Set;

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
