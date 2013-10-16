package se.inera.certificate.migration.testutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Unit tests for the PersonnummerGenerator utility.
 * 
 * @author nikpet
 *
 */
public class PersonnummerGeneratorTest {
    
    @Test
    public void testGenerateRandom() {
        String res = PersonnummerGenerator.generateRandomPersonnummer();
        assertNotNull(res);   
    }
    
    @Test
    public void testGenerateWithYear() {
        String res = PersonnummerGenerator.generatePersonnummer(1934, null, null);
        assertNotNull(res);
        assertEquals("1934", res.substring(0,4));
    }
    
    @Test
    public void testGenerateWithYearMonth() {
        String res = PersonnummerGenerator.generatePersonnummer(1934, 8, null);
        assertNotNull(res);
        assertEquals("193408", res.substring(0,6));
    }
    
    @Test
    public void testGenerateWithYearMonthDay() {
        String res = PersonnummerGenerator.generatePersonnummer(1934, 1, 10);
        assertNotNull(res);
        assertEquals("19340110", res.substring(0,8));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGenerateWrongMonth() {
        String res = PersonnummerGenerator.generatePersonnummer(1934, 13, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGenerateWrongDay() {
        String res = PersonnummerGenerator.generatePersonnummer(1934, 1, 33);
    }
    
    @Test()
    public void testGenerateWithLeapYear() {
        String res = PersonnummerGenerator.generatePersonnummer(2000, 2, 29);
        assertNotNull(res);
        assertEquals("20000229", res.substring(0,8));
    }
}
