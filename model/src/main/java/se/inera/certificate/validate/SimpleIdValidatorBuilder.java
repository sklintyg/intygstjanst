package se.inera.certificate.validate;

import org.joda.time.LocalDate;

/**
 * A simple builder of {@link IdValidator}s supporting {@link RootValidator}s without the need for dependency injection.
 * For more complex configuration of the IdValidator, use the spring configuration.
 * 
 * @author Gustav Norbäcker, R2M
 */
public class SimpleIdValidatorBuilder {

	/** The validator beeing built. */
	private IdValidator validator;

	public SimpleIdValidatorBuilder() {
		validator = new IdValidator();
	}

	/**
	 * Adds support for validation of 'personnummer' to the IdValidator.
	 * 
	 * @param strictSeparatorCheck
	 *            <code>true</code> if strict validation of the separator ('-' or '+') should be performed,
	 *            <code>false</code> otherwise.
	 * 
	 * @return This builder, for method chaining.
	 */
	public SimpleIdValidatorBuilder withPersonnummerValidator(boolean strictSeparatorCheck) {
		return withPersonnummerValidator(null, strictSeparatorCheck);
	}

	/**
	 * Adds support for validation of 'personnummer' to the IdValidator.
	 * 
	 * @param referenceDate
	 *            The date that should represent today. Set to <code>null</code> if the current date should be used.
	 * @param strictSeparatorCheck
	 *            <code>true</code> if strict validation of the separator ('-' or '+') should be performed,
	 *            <code>false</code> otherwise.
	 * 
	 * @return This builder, for method chaining.
	 */
	public SimpleIdValidatorBuilder withPersonnummerValidator(LocalDate referenceDate, boolean strictSeparatorCheck) {
		PersonnummerValidator newValidator = new PersonnummerValidator();
		newValidator.setReferenceDate(referenceDate);
		newValidator.setStrictSeparatorCheck(strictSeparatorCheck);

		validator.registerValidator(newValidator);

		return this;
	}

	/**
	 * Adds support for validation of 'samordningsnummer' to the IdValidator.
	 * 
	 * @param strictSeparatorCheck
	 *            <code>true</code> if strict validation of the separator ('-' or '+') should be performed,
	 *            <code>false</code> otherwise.
	 * 
	 * @return This builder, for method chaining.
	 */
	public SimpleIdValidatorBuilder withSamordningsnummerValidator(boolean strictSeparatorCheck) {
		return withSamordningsnummerValidator(null, strictSeparatorCheck);
	}

	/**
	 * Adds support for validation of 'samordningsnummer' to the IdValidator.
	 * 
	 * @param referenceDate
	 *            The date that should represent today. Set to <code>null</code> if the current date should be used.
	 * @param strictSeparatorCheck
	 *            <code>true</code> if strict validation of the separator ('-' or '+') should be performed,
	 *            <code>false</code> otherwise.
	 * 
	 * @return This builder, for method chaining.
	 */
	public SimpleIdValidatorBuilder withSamordningsnummerValidator(LocalDate referenceDate, boolean strictSeparatorCheck) {
		SamordningsnummerValidator newValidator = new SamordningsnummerValidator();
		newValidator.setReferenceDate(referenceDate);
		newValidator.setStrictSeparatorCheck(strictSeparatorCheck);

		validator.registerValidator(newValidator);

		return this;
	}
	
    /**
     * Adds support for validation of HSA id to the IdValidator.
     * 
     * @return This builder, for method chaining.
     */
    public SimpleIdValidatorBuilder withHsaIdValidator() {
        HsaIdValidator newValidator = new HsaIdValidator();

        validator.registerValidator(newValidator);

        return this;
    }

	/**
	 * Returns an instance of the {@link IdValidator} being built.
	 * 
	 * @return A new {@link IdValidator} instance.
	 */
	public IdValidator build() {
		IdValidator result = validator;
		validator = new IdValidator();
		return result;
	}
}
