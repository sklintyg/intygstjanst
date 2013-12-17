package se.inera.certificate.integration.validator;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.insuranceprocess.healthreporting.medcertqa._1.LakarutlatandeEnkelType;
import se.inera.certificate.logging.LogMarkers;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;

import com.google.common.base.Joiner;

/**
 * @author andreaskaltenbach
 */
public class LakarutlatandeEnkelTypeValidator {
    private static final Logger LOG = LoggerFactory.getLogger(LakarutlatandeEnkelTypeValidator.class);

    private LakarutlatandeEnkelType lakarutlatandeEnkelType;
    private List<String> validationErrors = null;

    private static final List<String> PATIENT_ID_OIDS = asList("1.2.752.129.2.1.3.1", "1.2.752.129.2.1.3.3");

    private static final String PERSON_NUMBER_REGEX = "[0-9]{8}[-+]?[0-9]{4}";
    private static final String PERSON_NUMBER_WITHOUT_DASH_REGEX = "[0-9]{12}";

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
        if (patient == null) {
            validationErrors.add("No Patient element found!");
            return;
        }

        // Check patient id - mandatory
        if (patient.getPersonId().getExtension() == null
                || patient.getPersonId().getExtension().isEmpty()) {
            validationErrors.add("No Patient Id found!");
            return;
        }
        //Correct personnummer without dashes
        String personNumber = patient.getPersonId().getExtension();
        if (personNumber.length() == 12 && Pattern.matches(PERSON_NUMBER_WITHOUT_DASH_REGEX, personNumber)) {
            patient.getPersonId().setExtension(personNumber.substring(0,8) + "-" + personNumber.substring(8));
            LOG.info(LogMarkers.VALIDATION, "Validation warning for intyg " + lakarutlatandeEnkelType.getLakarutlatandeId() + ": Person-id " + personNumber + " is lacking a separating dash - corrected.");
        }
        // Check patient o.i.d.
        if (patient.getPersonId().getRoot() == null || !PATIENT_ID_OIDS.contains(patient.getPersonId().getRoot())) {
            validationErrors.add(String.format("Wrong o.i.d. for Patient Id! Should be %s", Joiner.on(" or ").join(PATIENT_ID_OIDS)));
        }

        // Check format of patient id (has to be a valid personnummer)
        if (personNumber == null || !Pattern.matches(PERSON_NUMBER_REGEX, personNumber)) {
            validationErrors.add("Wrong format for person-id! Valid format is YYYYMMDD-XXXX or YYYYMMDD+XXXX.");
        }

        // Get namn for patient - mandatory
        if (patient.getFullstandigtNamn() == null || patient.getFullstandigtNamn().isEmpty()) {
            validationErrors.add("No Patient fullstandigtNamn elements found or set!");
        }
    }

}
