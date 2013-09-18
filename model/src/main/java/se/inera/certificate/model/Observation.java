package se.inera.certificate.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Observation {

    private Kod observationsKategori;
    private Kod observationsKod;
    private PartialInterval observationsPeriod;
    private String beskrivning;
    private List<Prognos> prognoser = new ArrayList<>();
    private List<PhysicalQuantity> varde;
    private Utforarroll utforsAv;

    public Kod getObservationsKategori() {
        return observationsKategori;
    }

    public void setObservationsKategori(Kod observationsKategori) {
        this.observationsKategori = observationsKategori;
    }

    public Kod getObservationsKod() {
        return observationsKod;
    }

    public void setObservationsKod(Kod observationsKod) {
        this.observationsKod = observationsKod;
    }

    public PartialInterval getObservationsPeriod() {
        return observationsPeriod;
    }

    public void setObservationsPeriod(PartialInterval observationsPeriod) {
        this.observationsPeriod = observationsPeriod;
    }

    public String getBeskrivning() {
        return beskrivning;
    }

    public void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

    public List<Prognos> getPrognoser() {
        return prognoser;
    }

    public void setPrognoser(List<Prognos> prognoser) {
        this.prognoser = prognoser;
    }

    public List<PhysicalQuantity> getVarde() {
        return varde;
    }

    public void setVarde(List<PhysicalQuantity> varde) {
        this.varde = varde;
    }

    public Utforarroll getUtforsAv() {
        return utforsAv;
    }

    public void setUtforsAv(Utforarroll utforsAv) {
        this.utforsAv = utforsAv;
    }
}
