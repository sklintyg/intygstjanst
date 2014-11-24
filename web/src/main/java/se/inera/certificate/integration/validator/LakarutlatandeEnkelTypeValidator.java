package se.inera.certificate.integration.validator;

import java.util.List;

import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.ifv.insuranceprocess.healthreporting.validate.PatientValidator;
import se.inera.webcert.medcertqa.v1.LakarutlatandeEnkelType;

/**
 * @author andreaskaltenbach
 */
public class LakarutlatandeEnkelTypeValidator {

    private LakarutlatandeEnkelType lakarutlatandeEnkelType;
    private List<String> validationErrors = null;

    public LakarutlatandeEnkelTypeValidator(LakarutlatandeEnkelType lakarutlatande, List<String> validationErrors) {
        this.lakarutlatandeEnkelType = lakarutlatande;
        this.validationErrors = validationErrors;
    }

    public void validateAndCorrect() {

        if (lakarutlatandeEnkelType.getLakarutlatandeId() == null || lakarutlatandeEnkelType.getLakarutlatandeId().isEmpty()) {
            validationErrors.add("No Lakarutlatande Id found!");
        }

        if (lakarutlatandeEnkelType.getSigneringsTidpunkt() == null) {
            validationErrors.add("No signeringstidpunkt found!");
        }

        validateAndCorrectPatient();
    }

    private void validateAndCorrectPatient() {
        PatientType patient = lakarutlatandeEnkelType.getPatient();
        if (PatientValidator.validateAndCorrect(lakarutlatandeEnkelType.getLakarutlatandeId(), patient, validationErrors)) {

            // Get namn for patient - mandatory
            if (patient.getFullstandigtNamn() == null || patient.getFullstandigtNamn().isEmpty()) {
                validationErrors.add("No Patient fullstandigtNamn elements found or set!");
            }
        }
    }

}
