package se.inera.intyg.intygstjanst.web.service.repo;

import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

import java.util.List;

public interface RecipientRepo {
    Recipient getRecipientForLogicalAddress(String logicalAddress) throws RecipientUnknownException;

    Recipient getRecipient(String recipientId) throws RecipientUnknownException;

    List<Recipient> listRecipients();

    Recipient getRecipientFkassa();

    Recipient getRecipientInvana();

    Recipient getRecipientHsvard();

    Recipient getRecipientTransp();
}
