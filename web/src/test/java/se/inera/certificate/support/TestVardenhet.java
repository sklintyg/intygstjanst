package se.inera.certificate.support;

import se.inera.certificate.model.Vardenhet;
import se.inera.certificate.model.Vardgivare;

public class TestVardenhet extends Vardenhet {

    private Vardgivare vardgivare;

    @Override
    public Vardgivare getVardgivare() {
        return vardgivare;
    }

    public void setVardgivare(Vardgivare vardgivare) {
        this.vardgivare = vardgivare;
    }
}
