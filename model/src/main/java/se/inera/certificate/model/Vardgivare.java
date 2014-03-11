package se.inera.certificate.model;

public class Vardgivare {

    private Id id;

    private String namn;

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
}
