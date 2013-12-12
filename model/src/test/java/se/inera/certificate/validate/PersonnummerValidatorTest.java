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
public class PersonnummerValidatorTest {

	/** The validator to test. */
	private PersonnummerValidator validator;

	@Before
	public void setup() {
		validator = new PersonnummerValidator();
		// Set a fixed date for the validator so test don't break in the future.
		validator.setReferenceDate(new LocalDate("2013-08-22"));
	}

	/**
	 * Test that only personnummer on the form <code>yyyyMMdd-nnnn</code> are accepted.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPersonnummerParser() throws Exception {
		assertListSize(0, validator.validateExtension("19800131-0005"));

		assertListSize(1, validator.validateExtension("800131-0005"));
		assertListSize(1, validator.validateExtension("198001310005"));
		assertListSize(1, validator.validateExtension("19800131-000x"));
	}

	/**
	 * Test that only valid dates are accepted by the validator.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPersonnummerDate() throws Exception {
		assertListSize(0, validator.validateExtension("19800131-0005"));
		assertListSize(0, validator.validateExtension("19800229-0008"));

		assertListSize(1, validator.validateExtension("19810229-0007"));
	}

	/**
	 * Test that only dates in the supported range are ok.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPersonnummerDateRange() throws Exception {
		assertListSize(0, validator.validateExtension("20130822-0001"));
		assertListSize(0, validator.validateExtension("18400506+0001"));

		assertListSize(1, validator.validateExtension("20130823-0000"));
		assertListSize(1, validator.validateExtension("18400505+0002"));
	}

	/**
	 * Test that the separator is validated correctly.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPersonnummerSeparator() throws Exception {
		assertListSize(0, validator.validateExtension("19090228+9818"));
		assertListSize(0, validator.validateExtension("19231213-9195"));

		assertListSize(1, validator.validateExtension("19090228-9818"));
		assertListSize(1, validator.validateExtension("19231213+9195"));
	}

	/**
	 * Test that the checksum is validated correctly.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPersonnummerChecksum() throws Exception {
		assertListSize(1, validator.validateExtension("19800131-0000"));
		assertListSize(1, validator.validateExtension("19800131-0001"));
		assertListSize(1, validator.validateExtension("19800131-0002"));
		assertListSize(1, validator.validateExtension("19800131-0003"));
		assertListSize(1, validator.validateExtension("19800131-0004"));
		assertListSize(0, validator.validateExtension("19800131-0005")); // This is the valid one!
		assertListSize(1, validator.validateExtension("19800131-0006"));
		assertListSize(1, validator.validateExtension("19800131-0007"));
		assertListSize(1, validator.validateExtension("19800131-0008"));
		assertListSize(1, validator.validateExtension("19800131-0009"));
	}

	private void assertListSize(int size, List<String> collection) {
		String validationMessage = Strings.join(",", collection);
		Assert.assertEquals(validationMessage, size, collection.size());
	}
}
