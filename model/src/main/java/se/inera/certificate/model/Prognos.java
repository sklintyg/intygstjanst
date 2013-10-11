package se.inera.certificate.model;

/**
 * @author andreaskaltenbach
 */
public class Prognos {

    private Kod prognoskod;
    private String beskrivning;

    public Kod getPrognoskod() {
        return prognoskod;
    }

    public void setPrognoskod(Kod prognoskod) {
        this.prognoskod = prognoskod;
    }

    public String getBeskrivning() {
        return beskrivning;
    }

    public void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }
}
