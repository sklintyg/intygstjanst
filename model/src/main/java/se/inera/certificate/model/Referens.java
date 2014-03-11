package se.inera.certificate.model;

import org.joda.time.LocalDate;

public class Referens {

    private Kod referenstyp;

    private LocalDate datum;

    public final Kod getReferenstyp() {
        return referenstyp;
    }

    public final void setReferenstyp(Kod referenstyp) {
        this.referenstyp = referenstyp;
    }

    public final LocalDate getDatum() {
        return datum;
    }

    public final void setDatum(LocalDate datum) {
        this.datum = datum;
    }
}
