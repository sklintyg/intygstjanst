package se.inera.certificate.validate;

import org.joda.time.LocalDate;

/**
 * Performs validation of a 'Samordningsnummer'.
 * 
 * @see PersonnummerValidator
 * @author Gustav Norbäcker, R2M
 */
public class SamordningsnummerValidator extends PersonnummerValidator {

	/** The root for samordningsnummer. */
	public static final String SAMORDNINGSNUMMER_ROOT = "1.2.752.129.2.1.3.3";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRoot() {
		return SAMORDNINGSNUMMER_ROOT;
	}

	/**
	 * Samordningsnummer have 60 added to the day. In order to calculate the birth day of the citizen, this needs to be
	 * substracted.
	 */
	protected LocalDate getBirthDay(String dateString) throws IllegalArgumentException {
		StringBuilder sb = new StringBuilder(dateString);
		String substractedWith6 = String.valueOf((char) (sb.charAt(6) - 6));
		sb.replace(6, 7, substractedWith6);

		return super.getBirthDay(sb.toString());
	}
}
