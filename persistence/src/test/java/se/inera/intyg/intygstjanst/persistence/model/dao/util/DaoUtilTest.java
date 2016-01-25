package se.inera.intyg.intygstjanst.persistence.model.dao.util;

import static org.junit.Assert.*;

import org.junit.Test;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

public class DaoUtilTest {

    @Test
    public void testWithDash() {
        Personnummer pnr = new Personnummer("19121212-1212");
        String expected = "19121212-1212";
        assertEquals(expected, DaoUtil.formatPnrForPersistence(pnr));
    }

    @Test
    public void testWithoutDash() {
        Personnummer pnr = new Personnummer("191212121212");
        String expected = "19121212-1212";
        assertEquals(expected, DaoUtil.formatPnrForPersistence(pnr));
    }

    @Test
    public void testWith6Digits() {
        Personnummer pnr = new Personnummer("1212121212");
        String expected = "20121212-1212";
        assertEquals(expected, DaoUtil.formatPnrForPersistence(pnr));
    }

}
