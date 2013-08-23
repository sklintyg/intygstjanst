package se.inera.certificate.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Performs validation of a 'Personnummer'. The following can be configured:
 * <ul>
 * <li>Strict dash check: If the dash should be validated. If the citizen is 100 years old or more, the separator should
 * be <code>+</code>, otherwise <code>-</code>.
 * <li>Reference date: By default all personnummer are validated against the current date. Other dates can be used by
 * setting {@link #setToday(LocalDate)}. Useful for unit testing.
 * </ul>
 * 
 * @author Gustav Norbäcker, R2M
 */
public class PersonnummerValidator implements RootValidator {

	/** The root for personnummer. */
	public static final String PERSONNUMMER_ROOT = "1.2.752.129.2.1.3.1";

	/** The regex pattern that the personnummer must conform to. */
	private static final Pattern PERSONNUMMER_PATTERN = Pattern.compile("(\\d{8})([+-])(\\d{3})(\\d)");

	/** This oldest citizen with a personnummer was born 1840-05-06 */
	private static final LocalDate FIRST_PERSONNUMMER_DATE = new LocalDate("1840-05-06");

	/** Should strict separator validation be performed? */
	private boolean strictSeparatorCheck = true;

	/** The reference date to use. <code>null</code> means today. */
	private LocalDate referenceDate = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRoot() {
		return PERSONNUMMER_ROOT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> validateExtension(String pnr) {
		List<String> result = new ArrayList<>();

		// Parse the personnummer
		Matcher m = PERSONNUMMER_PATTERN.matcher(pnr);
		if (!m.matches()) {
			result.add(String.format("Could not parse the SSN '%s' (format should be 'yyyyMMdd-nnnn')", pnr));
			return result;
		}
		String dateString = m.group(1);
		String separator = m.group(2);
		String nnn = m.group(3);
		int mod10 = Integer.parseInt(m.group(4));

		LocalDate birthday = checkDate(pnr, dateString, result);

		if (birthday != null) {
			checkDateRange(pnr, birthday, result);

			checkSeparator(pnr, birthday, separator, result);

			checkChecksum(pnr, dateString, nnn, mod10, result);
		}

		return result;
	}

	/**
	 * Check that the date is valid.
	 * 
	 * @param pnr
	 *            The personnummer. Used in validation messages.
	 * @param dateString
	 *            The date as a string at the form <code>yyyyMMdd</code>.
	 * @param result
	 *            List that validation messages are added to.
	 * 
	 * @return The birthday of the citizen, or <code>null</code> if the date could not be parsed.
	 */
	private LocalDate checkDate(String pnr, String dateString, List<String> result) {
		LocalDate localDate = null;

		try {
			localDate = getBirthDay(dateString);

		} catch (IllegalArgumentException e) {
			result.add(String.format("The date '%s' in SSN '%s' is invalid", dateString, pnr));
		}

		return localDate;
	}

	/**
	 * Check that the date is in a valid range. It must not be older that {@link #FIRST_PERSONNUMMER_DATE} or later than
	 * {@link #referenceDate}.
	 * 
	 * @param pnr
	 *            The personnummer. Used in validation messages.
	 * @param birthday
	 *            The date of the personnummer to check.
	 * @param result
	 *            List that validation messages are added to.
	 */
	private void checkDateRange(String pnr, LocalDate birthday, List<String> result) {
		if (birthday.isAfter(referenceDate())) {
			result.add("The SSN '%s' is invalid - date is in the future");
		}

		if (birthday.isBefore(FIRST_PERSONNUMMER_DATE)) {
			result.add("The SSN '%s' is invalid - date is too far in the past");
		}
	}

	/**
	 * Check that the separator of the personnummer is correct.
	 * 
	 * @param pnr
	 *            The personnummer. Used in validation messages.
	 * @param birthday
	 *            The date of the personnummer to check.
	 * @param separator
	 *            The separator of the personnummer to check.
	 * @param result
	 *            List that validation messages are added to.
	 */
	private void checkSeparator(String pnr, LocalDate birthday, String separator, List<String> result) {
		if (strictSeparatorCheck) {
			Period age = new Period(birthday, referenceDate());
			boolean ageMoreThan100 = age.getYears() >= 100;
			boolean dashSeparator = separator.equals("-");
			if (ageMoreThan100 && dashSeparator) {
				result.add(String.format(
						"The SSN '%s' is invalid - citizen is over 100 years old but the separator '-' is used", pnr));
			}
			if (!ageMoreThan100 && !dashSeparator) {
				result.add(String.format(
						"The SSN '%s' is invalid - citizen is under 100 years old but the separator '+' is used", pnr));
			}
		}
	}

	/**
	 * Check that the checksum of the personnummer is correct.
	 * 
	 * @param pnr
	 *            The personnummer. Used in validation messages.
	 * @param dateString
	 *            The date as a string at the form <code>yyyyMMdd</code>.
	 * @param nnn
	 *            The 3 first digits of the last 4.
	 * @param mod10
	 *            The last digit of the personnummer.
	 * @param result
	 *            List that validation messages are added to.
	 */
	private void checkChecksum(String pnr, String dateString, String nnn, int mod10, List<String> result) {
		if (ValidatorUtils.calculateMod10(dateString.substring(2) + nnn) != mod10) {
			result.add(String.format("The checksum digit in SSN '%s' is invalid", pnr));
		}
	}

	/**
	 * Returns the date of reference we want to use when validating the personnummer.
	 * 
	 * @return The reference date set with {@link #setReferenceDate(LocalDate)} or todays date owtherwise.
	 */
	private LocalDate referenceDate() {
		if (referenceDate == null) {
			return LocalDate.now();
		}
		return referenceDate;
	}

	/**
	 * This is the only calculation that differs between 'personnummer' and 'samordningsnummer'. Doing the calculation
	 * in a separate method makes it overridable.
	 * 
	 * @param dateString
	 *            The date string to convert into a birth day.
	 * 
	 * @return The date of birth of the citizen.
	 * 
	 * @throws IllegalArgumentException
	 *             if the date wasn't valid.
	 */
	protected LocalDate getBirthDay(String dateString) throws IllegalArgumentException {
		return ISODateTimeFormat.basicDate().parseLocalDate(dateString);
	}

	public boolean isStrictSeparatorCheck() {
		return strictSeparatorCheck;
	}

	public void setStrictSeparatorCheck(boolean strictSeparatorCheck) {
		this.strictSeparatorCheck = strictSeparatorCheck;
	}

	public LocalDate getReferenceDate() {
		return referenceDate;
	}

	public void setReferenceDate(LocalDate referenceDate) {
		this.referenceDate = referenceDate;
	}
}
