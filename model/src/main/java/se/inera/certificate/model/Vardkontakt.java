package se.inera.certificate.model;

public class Vardkontakt {

    private Kod vardkontakttyp;

    private LocalDateInterval vardkontaktstid;

    public final Kod getVardkontakttyp() {
        return vardkontakttyp;
    }

    public final void setVardkontakttyp(Kod vardkontakttyp) {
        this.vardkontakttyp = vardkontakttyp;
    }

    public final LocalDateInterval getVardkontaktstid() {
        return vardkontaktstid;
    }

    public final void setVardkontaktstid(LocalDateInterval vardkontaktstid) {
        this.vardkontaktstid = vardkontaktstid;
    }
}
