package se.inera.certificate.support;

import java.util.ArrayList;
import java.util.List;

import se.inera.certificate.model.Arbetsuppgift;
import se.inera.certificate.model.Patient;
import se.inera.certificate.model.Sysselsattning;

public class TestPatient extends Patient {

    private List<Sysselsattning> sysselsattningar;

    private List<Arbetsuppgift> arbetsuppgifter;

    @Override
    public List<Sysselsattning> getSysselsattningar() {
        if (sysselsattningar == null) {
            sysselsattningar = new ArrayList<>();
        }
        return sysselsattningar;
    }

    @Override
    public List<Arbetsuppgift> getArbetsuppgifter() {
        if (arbetsuppgifter == null) {
            arbetsuppgifter = new ArrayList<>();
        }
        return arbetsuppgifter;
    }
}
