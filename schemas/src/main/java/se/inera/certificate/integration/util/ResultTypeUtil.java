package se.inera.certificate.integration.util;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;

/**
 * @author andreaskaltenbach
 */
public final class ResultTypeUtil {

    private ResultTypeUtil() {
    }

    public static ResultType okResult() {
        ResultType result = new ResultType();
        result.setResultCode(ResultCodeType.OK);
        return result;
    }

    public static ResultType infoResult(String resultText) {
        ResultType result = new ResultType();
        result.setResultCode(ResultCodeType.INFO);
        result.setResultText(resultText);
        return result;
    }

    public static ResultType errorResult(ErrorIdType errorId, String resultText) {
        ResultType result = new ResultType();
        result.setResultCode(ResultCodeType.ERROR);
        result.setErrorId(errorId);
        result.setResultText(resultText);
        return result;
    }
}
