package se.inera.certificate.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs validation of a HSA id on the form 'SE[16]xxxxxxxxxx-nnnn'. The following rules are checked:
 * <ul>
 * <li>The id starts with 'SE'
 * <li>The organisationsnummer is either 10 digits or 12 digits and starting with '16'
 * <li>The organisationsnummer control digit is correct
 * <li>The local id ('nnnn') only contains charactes from the PRINTABLE_STRING class
 * <li>The total length of the HSA id doesn't exceed 31 characters.
 * </ul>
 */
public class HsaIdValidator implements RootValidator {

    /** The root of HSA-ids */
    private static final String HSA_ROOT = "1.2.752.129.2.1.4.1";

    /** Regex pattern HSA-ids should conform to */
    private static final Pattern HSA_VALID_PATTERN = Pattern.compile("SE(?:16)?([0-9]{10})\\-(.*)");

    /** Regex validating that a local id only got characters from the PRINTABLE_STRING class */
    private static final Pattern LOCAL_ID_VALID_PATTERN = Pattern
            .compile("[0-9A-Za-z \\'\\(\\)\\+\\,\\-\\.\\/\\:\\=\\?]*");

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRoot() {
        return HSA_ROOT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> validateExtension(String extension) {
        List<String> result = new ArrayList<String>();

        Matcher m = HSA_VALID_PATTERN.matcher(extension);

        if (!m.matches()) {
            result.add(String.format("HSA id '%s' doesn't match pattern SE[16]xxxxxxxxxx-n*", extension));
            return result;

        } else {
            String orgNo = m.group(1);
            String localId = m.group(2);
            checkOrgNumber(orgNo, result);
            checkLocalId(localId, result);
            checkHSALength(extension, result);
        }

        return result;
    }

    /**
     * Check that the total length of the HSA id doesn't exceed 31 characters.
     * 
     * @param extension
     *            The id to check.
     * @param result
     *            List that validation messages are added to.
     */
    private void checkHSALength(String extension, List<String> result) {
        if (extension.length() > 31) {
            result.add(String.format("HSA id '%s' exceeded the maximum length of 31 charcters", extension));
        }
    }

    /**
     * Check that the local id only contain accepted characters.
     * 
     * @param localId
     *            The local id to check.
     * @param result
     *            List that validation messages are added to.
     */
    private void checkLocalId(String localId, List<String> result) {
        if (!LOCAL_ID_VALID_PATTERN.matcher(localId).matches()) {
            result.add(String.format("Non valid character found in local id '%s'", localId));
        }
    }

    /**
     * Check that the organisationsnummer has a valid checksum.
     * 
     * @param orgNo
     *            The organisationsnummer to check.
     * @param result
     *            List that validation messages are added to.
     */
    private void checkOrgNumber(String orgNo, List<String> result) {
        String orgNoWithoutChecksum = orgNo.substring(0, 9);
        int mod10 = orgNo.charAt(9) - '0';

        if (ValidatorUtils.calculateMod10(orgNoWithoutChecksum) != mod10) {
            result.add(String.format("The checksum digit in org-number '%s' is invalid", orgNo));
        }
    }
}
