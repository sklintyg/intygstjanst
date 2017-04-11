package se.inera.intyg.intygstjanst.web.integration.util;

import java.util.Objects;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.CertificateStateHolder;

/**
 * Utility class containing business logic for what status removed for different consumers of the soap api.
 *
 * Created by carlf on 10/04/17.
 */
public final class CertificateStateFilterUtil {

    private CertificateStateFilterUtil() {
    }

    /**
     * A filters for status items, depending on consumer of the api (part) and the default recipient (huvudmottagare) of
     * the intyg type of the related intyg.
     *
     * @param status
     *            the status to be either kept or removed
     * @param part
     *            the consumer of the api
     * @param defaultRecipient
     *            huvudmottagare of the intyg type the status item belongs to
     * @return whether to keep or the given status item
     */
    public static boolean filter(CertificateStateHolder status, String part, String defaultRecipient) {
        switch (part) {
        case "INVANA":
            // Invanaren: alla statusar (INTYG-3629).
            return true;
        case "HSVARD":
            // Varden: alla statusar förutom Arkiverat, Aterstallt, Skickat (om part inte är en
            // huvudmottagare [default recipient]) (INTYG-3629)
            if (status.getState() == CertificateState.DELETED || status.getState() == CertificateState.RESTORED) {
                return false;
            }
            if (status.getState() == CertificateState.SENT && !Objects.equals(status.getTarget(), defaultRecipient)) {
                return false;
            }
            return true;
        default:
            // FKKASSA och ovriga parter
            if (status.getState() == CertificateState.DELETED || status.getState() == CertificateState.RESTORED) {
                return false;
            }
            if (status.getState() == CertificateState.SENT && !Objects.equals(part, status.getTarget())) {
                // Should not receive SENT-status items if consumer of api are not the recipient.
                return false;
            }
            return true;

        }
    }
}
