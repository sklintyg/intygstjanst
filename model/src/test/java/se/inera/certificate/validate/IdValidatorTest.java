package se.inera.certificate.validate;

import static junit.framework.Assert.*;

import org.joda.time.LocalDate;
import org.junit.Test;

import se.inera.certificate.model.Id;

public class IdValidatorTest {

	private static final LocalDate REFERENCE_DATE = new LocalDate("2013-08-22");

	private static final Id VALID_PERSONNUMMER = createPersonnummer("19800131-0005");
	private static final Id INVALID_PERSONNUMMER = createPersonnummer("19800131-0006");

	private static final Id VALID_SAMORDNINGSNUMMER = createSamordningsnummer("19800191-0002");
	private static final Id INVALID_SAMORDNINGSNUMMER = createSamordningsnummer("19800191-0003");

	@Test
	public void testPersonnummerIdValidator() throws Exception {
		IdValidator validator = new SimpleIdValidatorBuilder().withPersonnummerValidator(REFERENCE_DATE, true).build();

		assertEquals(true, validator.isValidationSupported(VALID_PERSONNUMMER));
		assertEquals(0, validator.validate(VALID_PERSONNUMMER).size());
		assertEquals(1, validator.validate(INVALID_PERSONNUMMER).size());

		assertEquals(false, validator.isValidationSupported(VALID_SAMORDNINGSNUMMER));
	}

	@Test
	public void testSamordningsnummerIdValidator() throws Exception {
		IdValidator validator = new SimpleIdValidatorBuilder().withSamordningsnummerValidator(REFERENCE_DATE, true)
				.build();

		assertEquals(false, validator.isValidationSupported(VALID_PERSONNUMMER));

		assertEquals(true, validator.isValidationSupported(VALID_SAMORDNINGSNUMMER));
		assertEquals(0, validator.validate(VALID_SAMORDNINGSNUMMER).size());
		assertEquals(1, validator.validate(INVALID_SAMORDNINGSNUMMER).size());
	}

	@Test
	public void testCompositeIdValidator() throws Exception {
		IdValidator validator = new SimpleIdValidatorBuilder().withPersonnummerValidator(REFERENCE_DATE, true)
				.withSamordningsnummerValidator(REFERENCE_DATE, true).build();

		assertEquals(true, validator.isValidationSupported(VALID_PERSONNUMMER));
		assertEquals(0, validator.validate(VALID_PERSONNUMMER).size());
		assertEquals(1, validator.validate(INVALID_PERSONNUMMER).size());

		assertEquals(true, validator.isValidationSupported(VALID_SAMORDNINGSNUMMER));
		assertEquals(0, validator.validate(VALID_SAMORDNINGSNUMMER).size());
		assertEquals(1, validator.validate(INVALID_SAMORDNINGSNUMMER).size());
	}

	private static Id createPersonnummer(String personnummer) {
		return new Id(PersonnummerValidator.PERSONNUMMER_ROOT, personnummer);
	}

	private static Id createSamordningsnummer(String samordningsnummer) {
		return new Id(SamordningsnummerValidator.SAMORDNINGSNUMMER_ROOT, samordningsnummer);
	}
}
