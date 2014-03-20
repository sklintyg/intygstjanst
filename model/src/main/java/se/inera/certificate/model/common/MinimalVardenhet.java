package se.inera.certificate.model.common;

import se.inera.certificate.model.Vardenhet;
import se.inera.certificate.model.Vardgivare;

public class MinimalVardenhet extends Vardenhet {

    private Vardgivare vardgivare;

    @Override
    public final Vardgivare getVardgivare() {
        return vardgivare;
    }

    public final void setVardgivare(Vardgivare vardgivare) {
        this.vardgivare = vardgivare;
    }
}
