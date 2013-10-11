package se.inera.certificate.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Observation {

    private Kod observationskategori;
    private Kod observationskod;
    private PartialInterval observationsperiod;
    private String beskrivning;
    private List<Prognos> prognoser;
    private List<PhysicalQuantity> varde;
    private Utforarroll utforsAv;

    public Kod getObservationskategori() {
        return observationskategori;
    }

    public void setObservationskategori(Kod observationskategori) {
        this.observationskategori = observationskategori;
    }

    public Kod getObservationskod() {
        return observationskod;
    }

    public void setObservationskod(Kod observationskod) {
        this.observationskod = observationskod;
    }

    public PartialInterval getObservationsperiod() {
        return observationsperiod;
    }

    public void setObservationsperiod(PartialInterval observationsperiod) {
        this.observationsperiod = observationsperiod;
    }

    public String getBeskrivning() {
        return beskrivning;
    }

    public void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

    public List<Prognos> getPrognoser() {
        if (prognoser == null) {
            prognoser = new ArrayList<>();
        }
        return prognoser;
    }

    public List<PhysicalQuantity> getVarde() {
        if (varde == null) {
            varde = new ArrayList<>();
        }
        return varde;
    }

    public Utforarroll getUtforsAv() {
        return utforsAv;
    }

    public void setUtforsAv(Utforarroll utforsAv) {
        this.utforsAv = utforsAv;
    }
}
