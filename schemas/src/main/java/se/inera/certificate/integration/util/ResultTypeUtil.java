package se.inera.certificate.integration.util;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;

/**
 * @author andreaskaltenbach
 */
public class ResultTypeUtil {

    private ResultTypeUtil() {
    }

    public static ResultType okResult() {
        ResultType result = new ResultType();
        result.setResultCode(ResultCodeType.OK);
        return result;
    }

    public static ResultType result(ResultCodeType resultCode, String resultText) {
        ResultType result = new ResultType();
        result.setResultCode(resultCode);
        result.setResultText(resultText);
        return result;
    }
}
