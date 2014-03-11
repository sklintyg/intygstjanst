package se.inera.certificate.support;

import se.inera.certificate.model.HosPersonal;
import se.inera.certificate.model.Vardenhet;

public class TestHosPersonal extends HosPersonal {

    private TestVardenhet vardenhet;

    @Override
    public Vardenhet getVardenhet() {
        return vardenhet;
    }

    public void setVardenhet(TestVardenhet vardenhet) {
        this.vardenhet = vardenhet;
    }
}
