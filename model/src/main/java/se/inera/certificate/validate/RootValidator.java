package se.inera.certificate.validate;

import java.util.List;

import se.inera.certificate.model.Id;

/**
 * Validator for a given <code>root</code> of an {@link Id} object.
 * 
 * @author Gustav Norbäcker, R2M
 */
public interface RootValidator {

	/**
	 * The root that this validator supports.
	 * 
	 * @return The name of the {@link Id} <code>root</code>.
	 */
	String getRoot();

	/**
	 * Performs validation of the {@link Id} <code>extension</code> of the <code>root</code> that this validator
	 * supports.
	 * 
	 * @param extension
	 *            The extension to validate.
	 * 
	 * @return A list of validation messages. An empty string if validation was successful.
	 */
	List<String> validateExtension(String extension);
}
