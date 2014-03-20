package se.inera.certificate.model.common;

import java.util.List;

import se.inera.certificate.model.Arbetsuppgift;
import se.inera.certificate.model.Patient;
import se.inera.certificate.model.PatientRelation;
import se.inera.certificate.model.Sysselsattning;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MinimalPatient extends Patient {

    @Override
    @JsonIgnore
    public List<? extends Sysselsattning> getSysselsattningar() {
        return super.getSysselsattningar();
    }

    @Override
    @JsonIgnore
    public List<? extends Arbetsuppgift> getArbetsuppgifter() {
        return super.getArbetsuppgifter();
    }

    @Override
    @JsonIgnore
    public List<? extends PatientRelation> getPatientrelationer() {
        return super.getPatientrelationer();
    }
}
