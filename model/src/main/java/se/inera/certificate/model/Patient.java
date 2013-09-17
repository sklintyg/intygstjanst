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

    private List<Sysselsattning> sysselsattnings;
    private List<Arbetsuppgift> arbetsuppgifts;
    private List<PatientRelation> patientrelations;

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public List<String> getFornamn() {
        return fornamn;
    }

    public void setFornamn(List<String> fornamn) {
        this.fornamn = fornamn;
    }

    public List<String> getMellannamn() {
        return mellannamn;
    }

    public void setMellannamn(List<String> mellannamn) {
        this.mellannamn = mellannamn;
    }

    public String getEfternamn() {
        return efternamn;
    }

    public void setEfternamn(String efternamn) {
        this.efternamn = efternamn;
    }

    public String getFullstandigtNamn() {
        List<String> names = new ArrayList<>();

        if (fornamn != null) {
            names.addAll(fornamn);
        }
        if (mellannamn != null) {
            names.addAll(mellannamn);
        }
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

    public List<Sysselsattning> getSysselsattnings() {
        return sysselsattnings;
    }

    public void setSysselsattnings(List<Sysselsattning> sysselsattnings) {
        this.sysselsattnings = sysselsattnings;
    }

    public List<Arbetsuppgift> getArbetsuppgifts() {
        return arbetsuppgifts;
    }

    public void setArbetsuppgifts(List<Arbetsuppgift> arbetsuppgifts) {
        this.arbetsuppgifts = arbetsuppgifts;
    }

	public List<PatientRelation> getPatientrelations() {
		return patientrelations;
	}

	public void setPatientrelations(List<PatientRelation> patientrelations) {
		this.patientrelations = patientrelations;
	}
}
