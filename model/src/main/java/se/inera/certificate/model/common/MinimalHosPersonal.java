package se.inera.certificate.model.common;

import se.inera.certificate.model.HosPersonal;
import se.inera.certificate.model.Vardenhet;

public class MinimalHosPersonal extends HosPersonal {

    private MinimalVardenhet vardenhet;

    @Override
    public final Vardenhet getVardenhet() {
        return vardenhet;
    }

    public final void setVardenhet(MinimalVardenhet vardenhet) {
        this.vardenhet = vardenhet;
    }
}
