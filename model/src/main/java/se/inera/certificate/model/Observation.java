package se.inera.certificate.model;

import java.util.ArrayList;
import java.util.List;

public class Observation {

    private Kod observationskategori;

    private Kod observationskod;

    private PartialInterval observationsperiod;

    private String beskrivning;

    private List<PhysicalQuantity> varde;

    public final Kod getObservationskategori() {
        return observationskategori;
    }

    public final void setObservationskategori(Kod observationskategori) {
        this.observationskategori = observationskategori;
    }

    public final Kod getObservationskod() {
        return observationskod;
    }

    public final void setObservationskod(Kod observationskod) {
        this.observationskod = observationskod;
    }

    public final PartialInterval getObservationsperiod() {
        return observationsperiod;
    }

    public final void setObservationsperiod(PartialInterval observationsperiod) {
        this.observationsperiod = observationsperiod;
    }

    public final String getBeskrivning() {
        return beskrivning;
    }

    public final void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

    public final List<PhysicalQuantity> getVarde() {
        if (varde == null) {
            varde = new ArrayList<>();
        }
        return varde;
    }

    public Utforarroll getUtforsAv() {
        return null;
    }
}
