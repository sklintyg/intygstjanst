package se.inera.certificate.schema.adapter;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author andreaskaltenbach
 */
public class PartialAdapterTest {

    private static final String YEAR = "2013";
    private static final String YEAR_MONTH = "2013-12";
    private static final String YEAR_MONTH_DAY = "2013-12-24";

    private static final Partial YEAR_PARTIAL = new Partial(DateTimeFieldType.year(), 2013);
    private static final Partial YEAR_MONTH_PARTIAL = YEAR_PARTIAL.with(DateTimeFieldType.monthOfYear(), 12);
    private static final Partial YEAR_MONTH_DAY_PARTIAL = YEAR_MONTH_PARTIAL.with(DateTimeFieldType.dayOfMonth(), 24);

    @Test
    public void testParseYear() {
        Partial partial = se.inera.certificate.schema.adapter.PartialAdapter.parsePartial(YEAR);
        assertEquals(YEAR_PARTIAL, partial);
    }

    @Test
    public void testParseYearMonth() {
        Partial partial = se.inera.certificate.schema.adapter.PartialAdapter.parsePartial(YEAR_MONTH);
        assertEquals(YEAR_MONTH_PARTIAL, partial);
    }

    @Test
    public void testParseYearMonthDay() {
        Partial partial = se.inera.certificate.schema.adapter.PartialAdapter.parsePartial(YEAR_MONTH_DAY);
        assertEquals(YEAR_MONTH_DAY_PARTIAL, partial);
    }

    @Test
    public void testPrintYear() {
        String date = se.inera.certificate.schema.adapter.PartialAdapter.printPartial(YEAR_PARTIAL);
        assertEquals(YEAR, date);
    }

    @Test
    public void testPrintYearMonth() {
        String date = se.inera.certificate.schema.adapter.PartialAdapter.printPartial(YEAR_MONTH_PARTIAL);
        assertEquals(YEAR_MONTH, date);
    }

    @Test
    public void testPrintYearMonthDay() {
        String date = se.inera.certificate.schema.adapter.PartialAdapter.printPartial(YEAR_MONTH_DAY_PARTIAL);
        assertEquals(YEAR_MONTH_DAY, date);
    }
}
