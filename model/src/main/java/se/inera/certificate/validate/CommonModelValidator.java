package se.inera.certificate.validate;

import java.util.ArrayList;
import java.util.List;

import se.inera.certificate.model.Utlatande;

/**
 * Placeholder for validation of common model that cannot practically be performed in schema validation.
 * 
 * @author marced
 * 
 */
public class CommonModelValidator {
    private Utlatande commonUtlatande;
    private List<String> validationErrors = new ArrayList<>();

    private static final String VALIDATION_ERROR_PREFIX = "Validation Error:";

    public CommonModelValidator(Utlatande commonUtlatande) {
        this.commonUtlatande = commonUtlatande;
    }

    public List<String> validate() {
        //TODO: add non-schema-validation rules as they appear
        return validationErrors;
    }

    private void addValidationError(String errorDesc) {
        validationErrors.add(VALIDATION_ERROR_PREFIX + errorDesc);
    }
}
