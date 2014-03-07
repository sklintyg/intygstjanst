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

    public final Id getId() {
        return id;
    }

    public final void setId(Id id) {
        this.id = id;
    }

    public final List<String> getFornamn() {
        if (fornamn == null) {
            fornamn = new ArrayList<>();
        }
        return fornamn;
    }

    public final List<String> getMellannamn() {
        if (mellannamn == null) {
            mellannamn = new ArrayList<>();
        }
        return mellannamn;
    }

    public final String getEfternamn() {
        return efternamn;
    }

    public final void setEfternamn(String efternamn) {
        this.efternamn = efternamn;
    }

    public String getFullstandigtNamn() {
        List<String> names = new ArrayList<>();

        names.addAll(getFornamn());
        names.addAll(getMellannamn());
        names.add(efternamn);

        return Strings.join(" ", names);
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

    public List<? extends Sysselsattning> getSysselsattningar() {
        if (sysselsattningar == null) {
            sysselsattningar = new ArrayList<>();
        }
        return sysselsattningar;
    }

    public List<? extends Arbetsuppgift> getArbetsuppgifter() {
        if (arbetsuppgifter == null) {
            arbetsuppgifter = new ArrayList<>();
        }
        return arbetsuppgifter;
    }

    public List<? extends PatientRelation> getPatientrelationer() {
        if (patientrelationer == null) {
            patientrelationer = new ArrayList<>();
        }
        return patientrelationer;
    }
}
