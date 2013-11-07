package se.inera.certificate.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HsaIdValidator implements RootValidator {

    /** The root of HSA-ids */
    private static final String HSA_ROOT = "1.2.752.129.2.1.4.1";

    /** Regex pattern HSA-ids should conform to */
    // TODO: Verify the actual spec of HSA-id, and change pattern accordingly
    private static final Pattern HSA_VALID_PATTERN = Pattern.compile("SE(?:16)?([0-9]{10})\\-(.*)");

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
            result.add("Invalid HSA-Id");
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

    private void checkHSALength(String extension, List<String> result) {
        if (extension.length() > 31) {
            result.add(String.format("HSA id '%s' exceeded the maximum length of 31 charcters", extension));
        }
    }

    private void checkLocalId(String localId, List<String> result) {
        if (!LOCAL_ID_VALID_PATTERN.matcher(localId).matches()) {
            result.add(String.format("Non valid character found in local id '%s'", localId));
        }
    }

    private void checkOrgNumber(String orgNo, List<String> result) {
        String orgNoWithoutChecksum = orgNo.substring(0, 9);
        int mod10 = orgNo.charAt(9) - '0';

        if (ValidatorUtils.calculateMod10(orgNoWithoutChecksum) != mod10) {
            result.add(String.format("The checksum digit in org-number '%s' is invalid", orgNo));
        }

    }

}
