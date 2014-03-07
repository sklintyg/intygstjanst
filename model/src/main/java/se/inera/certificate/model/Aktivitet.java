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

    public final Kod getAktivitetskod() {
        return aktivitetskod;
    }

    public final void setAktivitetskod(Kod aktivitetskod) {
        this.aktivitetskod = aktivitetskod;
    }

    public final String getBeskrivning() {
        return beskrivning;
    }

    public final void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

    public final PartialInterval getAktivitetstid() {
        return aktivitetstid;
    }

    public final void setAktivitetstid(PartialInterval aktivitetstid) {
        this.aktivitetstid = aktivitetstid;
    }

    public List<? extends Utforarroll> getBeskrivsAv() {
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
