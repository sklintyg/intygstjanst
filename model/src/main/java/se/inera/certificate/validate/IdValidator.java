package se.inera.certificate.validate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import se.inera.certificate.model.Id;

/**
 * Validator of {@link Id} objects. Depending on the <code>root</code> of the {@link Id} the <code>extension</code>
 * could possible be checked for validity. This validator handles registering different kinds of validators for
 * different roots.
 * 
 * @author Gustav Norbäcker, R2M
 */
public class IdValidator {

	/** Map of roots that support validation. */
	private final HashMap<String, RootValidator> validators;

	public IdValidator() {
		validators = new HashMap<>();
	}

	/**
	 * Validates an {@link Id} object, given that the <code>root</code> has a registered {@link RootValidator}.
	 * 
	 * @param id
	 *            The Id to validate. Should not be <code>null</code>.
	 * 
	 * @return A list of validation messages. An empty list if validation was successful.
	 */
	public List<String> validate(Id id) {
		RootValidator validator = validators.get(id.getRoot());
		if (validator == null) {
			return Collections.emptyList();
		}

		return validator.validateExtension(id.getExtension());
	}

	/**
	 * Checks if this validator has a registered {@link RootValidator} for a given {@link Id} <code>root</code>.
	 * 
	 * @param id
	 *            The Id to check.
	 * 
	 * @return <code>true</code> if validation is possible, <code>false</code> otherwise.
	 */
	public boolean isValidationSupported(Id id) {
		return validators.containsKey(id.getRoot());
	}

	/**
	 * Registers a new {@link RootValidator} with this validator.
	 * 
	 * @param validator
	 *            The root validator to register.
	 */
	public void registerValidator(RootValidator validator) {
		this.validators.put(validator.getRoot(), validator);
	}

	/**
	 * Register a whole list of {@link RootValidator}s. Useful for dependency injection.
	 * 
	 * @param validators
	 *            The validators to regsiter.
	 */
	public void setValidators(List<RootValidator> validators) {
		for (RootValidator validator : validators) {
			registerValidator(validator);
		}
	}
}
