package se.inera.certificate.model.common;

import java.util.List;

import se.inera.certificate.model.Aktivitet;
import se.inera.certificate.model.Observation;
import se.inera.certificate.model.Referens;
import se.inera.certificate.model.Rekommendation;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.Vardkontakt;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Concrete implementation of the generic domain model with a bare minimal of fields. Useful if an application needs to
 * deserialize JSON etc.
 * <p>
 * Consider extending {@link Utlatande} with your own implementation if anything more specific is needed.
 */
public class MinimalUtlatande extends Utlatande {

    private MinimalPatient patient;

    private MinimalHosPersonal skapadAv;

    @Override
    public MinimalPatient getPatient() {
        return patient;
    }

    public void setPatient(MinimalPatient patient) {
        this.patient = patient;
    }

    @Override
    public MinimalHosPersonal getSkapadAv() {
        return skapadAv;
    }

    public void setSkapadAv(MinimalHosPersonal skapadAv) {
        this.skapadAv = skapadAv;
    }

    @Override
    @JsonIgnore
    public List<? extends Aktivitet> getAktiviteter() {
        return super.getAktiviteter();
    }

    @Override
    @JsonIgnore
    public List<? extends Observation> getObservationer() {
        return super.getObservationer();
    }

    @Override
    @JsonIgnore
    public List<? extends Vardkontakt> getVardkontakter() {
        return super.getVardkontakter();
    }

    @Override
    @JsonIgnore
    public List<? extends Rekommendation> getRekommendationer() {
        return super.getRekommendationer();
    }

    @Override
    @JsonIgnore
    public List<? extends Referens> getReferenser() {
        return super.getReferenser();
    }
}
