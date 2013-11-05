package se.inera.certificate.integration.validator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;

public class SendCertificateRequestValidator {
    SendType sendRequest = null;
    private List<String> validationErrors = new ArrayList<>();

    public SendCertificateRequestValidator(SendType sendRequest) {
        this.sendRequest = sendRequest;
    }

    public void validateAndCorrect() {
        // First, validate properties at Revoke request level
        if (StringUtils.isEmpty(sendRequest.getVardReferensId())) {
            validationErrors.add("No vardReferens found!");
        }
        if (sendRequest.getAvsantTidpunkt() == null) {
            validationErrors.add("No avsantTidpunkt found!");
        }

        // use commmon validators for common elements
        new LakarutlatandeEnkelTypeValidator(sendRequest.getLakarutlatande(), validationErrors).validateAndCorrect();
        new VardAdresseringsTypeValidator(sendRequest.getAdressVard(), validationErrors).validateAndCorrect();

 
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
    }
}
