package se.inera.certificate.integration.validator;

import org.springframework.util.StringUtils;
import se.inera.certificate.validate.CertificateValidationException;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.validator.VardAdresseringsTypeValidator;

import java.util.ArrayList;
import java.util.List;

public class RevokeRequestValidator {
    private RevokeType revokeRequest = null;
    private List<String> validationErrors = new ArrayList<>();

    public RevokeRequestValidator(RevokeType revokeRequest) {
        this.revokeRequest = revokeRequest;
    }

    public void validateAndCorrect() throws CertificateValidationException {
        // First, validate properties at Revoke request level
        if (StringUtils.isEmpty(revokeRequest.getVardReferensId())) {
            validationErrors.add("No vardReferens found!");
        }
        if (revokeRequest.getAvsantTidpunkt() == null) {
            validationErrors.add("No avsantTidpunkt found!");
        }

        // use commmon validators for common elements
        new LakarutlatandeEnkelTypeValidator(revokeRequest.getLakarutlatande(), validationErrors).validateAndCorrect();
        new VardAdresseringsTypeValidator(revokeRequest.getAdressVard(), validationErrors).validateAndCorrect();

        if (!validationErrors.isEmpty()) {
            throw new CertificateValidationException(validationErrors);
        }
    }
}
