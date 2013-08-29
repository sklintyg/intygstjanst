package se.inera.certificate.model;
/** 
 * @author erik
 */
public class Observationssamband {

	private Kod sambandskod;
	private Observation till;
	private Observation fran;

	public Kod getSambandskod() {
		return sambandskod;
	}

	public void setSambandskod(Kod sambandskod) {
		this.sambandskod = sambandskod;
	}

	public Observation getTill() {
		return till;
	}

	public void setTill(Observation till) {
		this.till = till;
	}

	public Observation getFran() {
		return fran;
	}

	public void setFran(Observation fran) {
		this.fran = fran;
	}
}
