package se.inera.certificate.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Aktivitet {
    private Kod aktivitetskod;
    private String beskrivning;
    private PartialInterval aktivitetstid;
    private List<Utforarroll> beskrivsAv;
    private Vardenhet utforsVid;

    public Kod getAktivitetskod() {
        return aktivitetskod;
    }

    public void setAktivitetskod(Kod aktivitetskod) {
        this.aktivitetskod = aktivitetskod;
    }

    public String getBeskrivning() {
        return beskrivning;
    }

    public void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

    public PartialInterval getAktivitetstid() {
        return aktivitetstid;
    }

    public void setAktivitetstid(PartialInterval aktivitetstid) {
        this.aktivitetstid = aktivitetstid;
    }

    public List<Utforarroll> getBeskrivsAv() {
        if (beskrivsAv == null) {
            beskrivsAv = new ArrayList<Utforarroll>();
        }
        return this.beskrivsAv;
    }

    public Vardenhet getUtforsVid() {
        return utforsVid;
    }

    public void setUtforsVid(Vardenhet utforsVid) {
        this.utforsVid = utforsVid;
    }
}
