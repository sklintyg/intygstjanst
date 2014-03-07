package se.inera.certificate.model;

/**
 * @author andreaskaltenbach
 */
public class Prognos {

    private Kod prognoskod;
    private String beskrivning;

    public final Kod getPrognoskod() {
        return prognoskod;
    }

    public final void setPrognoskod(Kod prognoskod) {
        this.prognoskod = prognoskod;
    }

    public final String getBeskrivning() {
        return beskrivning;
    }

    public final void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }
}
