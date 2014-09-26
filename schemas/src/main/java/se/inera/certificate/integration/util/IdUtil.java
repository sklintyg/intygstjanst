package se.inera.certificate.integration.util;

import iso.v21090.dt.v1.II;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;

/**
 * A valid id can have two formats:
 * <ul>
 * <li>A <code>root</code> with the value {@value #UTLATANDE_ID_IOD} and an <code>extension</code> that (globally)
 * uniquely identifies the intyg.
 * <li>A <code>root</code> that (globally) uniquely identifies the intyg (<code>extension</code> must then be
 * <code>null</code>).
 * </ul>
 */
public class IdUtil {

    private static final String UTLATANDE_ID_IOD = "1.2.752.129.2.1.2.1";

    public static String generateStringId(II id) throws InvalidIdException {
        return generateStringId(id.getRoot(), id.getExtension());
    }

    public static String generateStringId(String root, String extension) throws InvalidIdException {
        if (root == null) {
            throw new InvalidIdException(root, extension);
        }

        if (root.equals(UTLATANDE_ID_IOD)) {
            if (extension != null) {
                return extension;

            } else {
                throw new InvalidIdException(root, extension);
            }

        } else {
            if (extension == null) {
                return root;

            } else {
                throw new InvalidIdException(root, extension);
            }
        }
    }

    public static UtlatandeId generateId(String id) throws InvalidIdException {
        if (id == null) {
            throw new InvalidIdException(id);
        }
        // A common mistake is to use the UTLATANDE_ID_OID as a id. We do not permit this in order to detect errors
        // earlier on.
        if (id.equals(UTLATANDE_ID_IOD)) {
            throw new InvalidIdException(id);
        }

        UtlatandeId result = new UtlatandeId();
        result.setRoot(UTLATANDE_ID_IOD);
        result.setExtension(id);
        return result;
    }
}
