package se.inera.certificate.model.common;

import se.inera.certificate.model.Patient;
import se.inera.certificate.model.Utlatande;

/**
 * Minimal concrete implementation of the generic domain model. Useful if an application needs to deserialize JSON etc.
 */
public class MinimalUtlatande extends Utlatande {

    private Patient patient;

    private MinimalHosPersonal skapadAv;

    @Override
    public final Patient getPatient() {
        return patient;
    }

    public final void setPatient(Patient patient) {
        this.patient = patient;
    }

    @Override
    public final MinimalHosPersonal getSkapadAv() {
        return skapadAv;
    }

    public final void setSkapadAv(MinimalHosPersonal skapadAv) {
        this.skapadAv = skapadAv;
    }
}
