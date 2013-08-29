package se.inera.certificate.model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

/**
 * @author andreaskaltenbach
 */
public class Observation {

	private Id observationsId;
	private Kod observationsKategori;
	private Kod observationsKod;
	private LocalDateTime observationsTid;
	private PartialInterval observationsPeriod;
	private String beskrivning;
	private Prognos prognos;
	private List<PhysicalQuantity> varde;
	private Boolean forekomst;
	private Boolean patientInstammer;
	private List<Utforarroll> utforsAv;
	private List<Observationssamband> observationssamband;

	public Id getObservationsId() {
		return observationsId;
	}

	public void setObservationsId(Id observationsId) {
		this.observationsId = observationsId;
	}

	public Kod getObservationsKategori() {
		return observationsKategori;
	}

	public void setObservationsKategori(Kod observationsKategori) {
		this.observationsKategori = observationsKategori;
	}

	public Kod getObservationsKod() {
		return observationsKod;
	}

	public void setObservationsKod(Kod observationsKod) {
		this.observationsKod = observationsKod;
	}

	public PartialInterval getObservationsPeriod() {
		return observationsPeriod;
	}

	public void setObservationsPeriod(PartialInterval observationsPeriod) {
		this.observationsPeriod = observationsPeriod;
	}

	public String getBeskrivning() {
		return beskrivning;
	}

	public void setBeskrivning(String beskrivning) {
		this.beskrivning = beskrivning;
	}

	public Prognos getPrognos() {
		return prognos;
	}

	public void setPrognos(Prognos prognos) {
		this.prognos = prognos;
	}

	public List<PhysicalQuantity> getVarde() {
		return varde;
	}

	public void setVarde(List<PhysicalQuantity> varde) {
		this.varde = varde;
	}

	public LocalDateTime getObservationsTid() {
		return observationsTid;
	}

	public void setObservationsTid(LocalDateTime observationsTid) {
		this.observationsTid = observationsTid;
	}

	public Boolean getForekomst() {
		return forekomst;
	}

	public void setForekomst(Boolean forekomst) {
		this.forekomst = forekomst;
	}

	public Boolean getPatientInstammer() {
		return patientInstammer;
	}

	public void setPatientInstammer(Boolean patientInstammer) {
		this.patientInstammer = patientInstammer;
	}

	public List<Utforarroll> getUtforsAv() {
		if (utforsAv == null) {
			utforsAv = new ArrayList<Utforarroll>();
		}
		return this.utforsAv;
	}

	public List<Observationssamband> getObservationssamband() {
		return observationssamband;
	}

	public void setObservationssamband(List<Observationssamband> observationssamband) {
		this.observationssamband = observationssamband;
	}
}
