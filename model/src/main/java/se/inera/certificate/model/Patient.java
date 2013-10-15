package se.inera.certificate.model;

import java.util.ArrayList;
import java.util.List;

import se.inera.certificate.model.util.Strings;

/**
 * @author andreaskaltenbach
 */
public class Patient {

    private Id id;
    private List<String> fornamn;
    private List<String> mellannamn;
    private String efternamn;

    private String postadress;
    private String postnummer;
    private String postort;

    private List<Sysselsattning> sysselsattningar;
    private List<Arbetsuppgift> arbetsuppgifter;
    private List<PatientRelation> patientrelationer;

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public List<String> getFornamn() {
        if (fornamn == null) {
            fornamn = new ArrayList<>();
        }
        return fornamn;
    }

    public List<String> getMellannamn() {
        if (mellannamn == null) {
            mellannamn = new ArrayList<>();
        }
        return mellannamn;
    }

    public String getEfternamn() {
        return efternamn;
    }

    public void setEfternamn(String efternamn) {
        this.efternamn = efternamn;
    }

    public String getFullstandigtNamn() {
        List<String> names = new ArrayList<>();

        names.addAll(getFornamn());
        names.addAll(getMellannamn());
        names.add(efternamn);

        return Strings.join(" ", names);
    }

    public String getPostadress() {
        return postadress;
    }

    public void setPostadress(String postadress) {
        this.postadress = postadress;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    public String getPostort() {
        return postort;
    }

    public void setPostort(String postort) {
        this.postort = postort;
    }

    public List<Sysselsattning> getSysselsattningar() {
        if (sysselsattningar == null) {
            sysselsattningar = new ArrayList<>();
        }
        return sysselsattningar;
    }

    public List<Arbetsuppgift> getArbetsuppgifter() {
        if (arbetsuppgifter == null) {
            arbetsuppgifter = new ArrayList<>();
        }
        return arbetsuppgifter;
    }

    public List<PatientRelation> getPatientrelationer() {
        if (patientrelationer == null) {
            patientrelationer = new ArrayList<>();
        }
        return patientrelationer;
    }
}
