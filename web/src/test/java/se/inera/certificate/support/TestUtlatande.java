package se.inera.certificate.support;

import java.util.ArrayList;
import java.util.List;

import se.inera.certificate.model.Aktivitet;
import se.inera.certificate.model.Observation;
import se.inera.certificate.model.Referens;
import se.inera.certificate.model.Rekommendation;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.Vardkontakt;

/**
 * Concrete implementation of the generic domain model. Used by tests that needs to instantiate the model.
 */
public class TestUtlatande extends Utlatande {

    private TestPatient patient;

    private TestHosPersonal skapadAv;

    private List<Aktivitet> aktiviteter;

    private List<Observation> observationer;

    private List<Vardkontakt> vardkontakter;

    private List<Rekommendation> rekommendationer;

    private List<Referens> referens;

    @Override
    public TestPatient getPatient() {
        return patient;
    }

    public void setPatient(TestPatient patient) {
        this.patient = patient;
    }

    @Override
    public TestHosPersonal getSkapadAv() {
        return skapadAv;
    }

    public void setSkapadAv(TestHosPersonal skapadAv) {
        this.skapadAv = skapadAv;
    }

    @Override
    public List<Aktivitet> getAktiviteter() {
        if (aktiviteter == null) {
            aktiviteter = new ArrayList<>();
        }
        return aktiviteter;
    }

    @Override
    public List<Observation> getObservationer() {
        if (observationer == null) {
            observationer = new ArrayList<>();
        }
        return observationer;
    }

    @Override
    public List<Vardkontakt> getVardkontakter() {
        if (vardkontakter == null) {
            vardkontakter = new ArrayList<>();
        }
        return vardkontakter;
    }

    @Override
    public List<Rekommendation> getRekommendationer() {
        if (rekommendationer == null) {
            rekommendationer = new ArrayList<>();
        }
        return rekommendationer;
    }

    @Override
    public List<Referens> getReferenser() {
        if (referens == null) {
            referens = new ArrayList<>();
        }
        return referens;
    }
}
