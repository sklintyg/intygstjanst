package se.inera.certificate.model;

/**
 * @author andreaskaltenbach
 */
public class HosPersonal {
    private Id id;
    private String namn;
    private String forskrivarkod;
    private String befattning;
    private Vardenhet vardenhet;

    public final Id getId() {
        return id;
    }

    public final void setId(Id id) {
        this.id = id;
    }

    public final String getNamn() {
        return namn;
    }

    public final void setNamn(String namn) {
        this.namn = namn;
    }

    public final String getForskrivarkod() {
        return forskrivarkod;
    }

    public final void setForskrivarkod(String forskrivarkod) {
        this.forskrivarkod = forskrivarkod;
    }

    public final String getBefattning() {
        return befattning;
    }

    public final void setBefattning(String befattning) {
        this.befattning = befattning;
    }

    public Vardenhet getVardenhet() {
        return vardenhet;
    }

    public void setVardenhet(Vardenhet vardenhet) {
        this.vardenhet = vardenhet;
    }
}
