package se.inera.certificate.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Aktivitet {
    private Id aktivitetsid;
	private Kod aktivitetskod;
    private String beskrivning;
    private PartialInterval aktivitetstid;
    private Kod aktivitetsstatus;
    private String motivering;
    private String syfte;
    private List<Utforarroll> utforsAv;
    private List<Utforarroll> beskrivsAv;
    private Vardenhet utforsVid;
    
    
    public List<Utforarroll> getUtforsAv(){
    	if (utforsAv == null){
    		utforsAv = new ArrayList<Utforarroll>();
    	}
    	return this.utforsAv;
    }
    
    public List<Utforarroll> getBeskrivsAv(){
    	if (beskrivsAv == null){
    		beskrivsAv = new ArrayList<Utforarroll>();
    	}
    	return this.beskrivsAv;
    }
    
    public Kod getAktivitetskod() {
        return aktivitetskod;
    }

    public void setAktivitetskod(Kod aktivitetskod) {
        this.aktivitetskod = aktivitetskod;
    }

    public String getBeskrivning() {
        return beskrivning;
    }

    public void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

	public PartialInterval getAktivitetstid() {
		return aktivitetstid;
	}

	public void setAktivitetstid(PartialInterval aktivitetstid) {
		this.aktivitetstid = aktivitetstid;
	}

	public Id getAktivitetsid() {
		return aktivitetsid;
	}

	public void setAktivitetsid(Id aktivitetsid) {
		this.aktivitetsid = aktivitetsid;
	}

	public Kod getAktivitetsstatus() {
		return aktivitetsstatus;
	}

	public void setAktivitetsstatus(Kod aktivitetsstatus) {
		this.aktivitetsstatus = aktivitetsstatus;
	}

	public String getMotivering() {
		return motivering;
	}

	public void setMotivering(String motivering) {
		this.motivering = motivering;
	}

	public String getSyfte() {
		return syfte;
	}

	public void setSyfte(String syfte) {
		this.syfte = syfte;
	}

	public Vardenhet getUtforsVid() {
		return utforsVid;
	}

	public void setUtforsVid(Vardenhet utforsVid) {
		this.utforsVid = utforsVid;
	}
}
