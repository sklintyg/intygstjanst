package se.inera.certificate.validate;

import java.util.List;

import junit.framework.Assert;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import se.inera.certificate.model.util.Strings;

/**
 * Test the {@link PersonnummerValidator}.
 * 
 * @author Gustav Norbäcker, R2M
 */
public class SamordningsnummerValidatorTest {

	/** The validator to test. */
	private SamordningsnummerValidator validator;

	@Before
	public void setup() {
		validator = new SamordningsnummerValidator();
		// Set a fixed date for the validator so test don't break in the future.
		validator.setReferenceDate(new LocalDate("2013-08-22"));
	}

	/**
	 * Test that only dates in the samordningsnummer series are supported.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPersonnummerDate() throws Exception {
		assertListSize(0, validator.validateExtension("19800191-0002"));
		assertListSize(0, validator.validateExtension("19800289-0005"));

		assertListSize(1, validator.validateExtension("19810289-0004"));
		assertListSize(1, validator.validateExtension("19800131-0005"));
		assertListSize(1, validator.validateExtension("19800229-0008"));
	}

	private void assertListSize(int size, List<String> collection) {
		String validationMessage = Strings.join(",", collection);
		Assert.assertEquals(validationMessage, size, collection.size());
	}
}
