package se.inera.certificate.model;

import java.util.Collections;
import java.util.List;

public class Aktivitet {

    private Kod aktivitetskod;

    private String beskrivning;

    private PartialInterval aktivitetstid;

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
        return Collections.emptyList();
    }

    public Vardenhet getUtforsVid() {
        return null;
    }
}
