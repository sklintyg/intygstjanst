package se.inera.certificate.model;

public abstract class Vardenhet {

    private Id id;

    private Id arbetsplatskod;

    private String namn;

    private String postadress;

    private String postnummer;

    private String postort;

    private String telefonnummer;

    private String epost;

    public final Id getId() {
        return id;
    }

    public final void setId(Id id) {
        this.id = id;
    }

    public final Id getArbetsplatskod() {
        return arbetsplatskod;
    }

    public final void setArbetsplatskod(Id arbetsplatskod) {
        this.arbetsplatskod = arbetsplatskod;
    }

    public final String getNamn() {
        return namn;
    }

    public final void setNamn(String namn) {
        this.namn = namn;
    }

    public final String getPostadress() {
        return postadress;
    }

    public final void setPostadress(String postadress) {
        this.postadress = postadress;
    }

    public final String getPostnummer() {
        return postnummer;
    }

    public final void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    public final String getPostort() {
        return postort;
    }

    public final void setPostort(String postort) {
        this.postort = postort;
    }

    public final String getTelefonnummer() {
        return telefonnummer;
    }

    public final void setTelefonnummer(String telefonnummer) {
        this.telefonnummer = telefonnummer;
    }

    public final String getEpost() {
        return epost;
    }

    public final void setEpost(String epost) {
        this.epost = epost;
    }

    public abstract Vardgivare getVardgivare();
}
