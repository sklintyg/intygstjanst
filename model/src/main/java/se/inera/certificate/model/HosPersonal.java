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

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public String getForskrivarkod() {
        return forskrivarkod;
    }

    public void setForskrivarkod(String forskrivarkod) {
        this.forskrivarkod = forskrivarkod;
    }

    public String getBefattning() {
        return befattning;
    }

    public void setBefattning(String befattning) {
        this.befattning = befattning;
    }

    public Vardenhet getVardenhet() {
        return vardenhet;
    }

    public void setVardenhet(Vardenhet vardenhet) {
        this.vardenhet = vardenhet;
    }
}