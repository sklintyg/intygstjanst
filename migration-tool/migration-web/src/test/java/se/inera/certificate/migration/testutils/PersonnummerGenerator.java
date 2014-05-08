package se.inera.certificate.migration.testutils;

import java.util.Random;

import org.apache.commons.lang.StringUtils;

/**
 * Utility for generating Personnummer.
 * 
 * @author nikpet
 *
 */
public final class PersonnummerGenerator {

    private PersonnummerGenerator() {
        
    }
    
    /**
     * Generates a totally random but valid personnummer.
     * 
     * @return
     */
    public static String generateRandomPersonnummer() {
        return generatePersonnummer(null, null, null);
    }

    /**
     * Generates a valid personnummer based on supplied year, month and day. 
     * All parameters can be null and is then substituted with a random value. 
     * 
     * @param year Year of birth, can be null.
     * @param month Month of birth, can be null.
     * @param day Day of birth, can be null.
     * @return
     */
    public static String generatePersonnummer(Integer year, Integer month, Integer day) {

        StringBuilder pnr = new StringBuilder();

        String datePart = calcDatePart(year, month, day);
        pnr.append(datePart).append("-");

        String serialPart = calcSerialPart();
        pnr.append(serialPart);

        String checkDigit = calcCheckDigit(datePart, serialPart);
        pnr.append(checkDigit);

        return pnr.toString();
    }

    private static String calcDatePart(Integer year, Integer month, Integer day) {

        year = (year != null) ? year : 1900 + randomInt(0, 100);

        month = (month != null) ? month : randomInt(1, 12);

        int maxDayLength = calcMaxDayLength(year, month);

        if (day != null) {
            if (day > maxDayLength) {
                throw new IllegalArgumentException("Bad number of days supplied!");
            }
        } else {
            day = randomInt(1, maxDayLength);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toString(year));
        sb.append(zeroPad(Integer.toString(month), 2));
        sb.append(zeroPad(Integer.toString(day), 2));

        return sb.toString();
    }

    private static int calcMaxDayLength(Integer year, Integer month) {

        switch (month) {
        case 1:
        case 3:
        case 5:
        case 7:
        case 8:
        case 10:
        case 12:
            return 31;
        case 4:
        case 6:
        case 9:
        case 11:
            return 30;
        case 2:
            if (((year % 4 == 0) && !(year % 100 == 0)) || (year % 400 == 0))
                return 29;
            else
                return 28;
        default:
            throw new IllegalArgumentException("Bad month number, should be between 1 and 12!");
        }

    }

    private static String calcSerialPart() {
        return zeroPad(Integer.toString(randomInt(1, 999)), 3);
    }

    private static String calcCheckDigit(String datePart, String serialPart) {

        // strip century digits from datePart and concat serial
        String number = datePart.substring(2).concat(serialPart);

        int cs = 0;
        int multiple = 2;
        for (int i = 0; i < number.length(); i++) {
            int code = Integer.parseInt(number.substring(i, i + 1));
            int pos = multiple * code;
            cs += pos % 10 + pos / 10;
            multiple = (multiple == 1 ? 2 : 1);
        }

        // Subtract the sum modulo 10 from 10.
        // The remainder becomes the checksum. If the remainder is 10 the
        // checksum i 0.
        int checkDigit = (10 - (cs % 10)) % 10;

        return Integer.toString(checkDigit);
    }

    private static String zeroPad(String str, int size) {
        return StringUtils.leftPad(str, size, "0");
    }

    private static int randomInt(int min, int max) {
        Random rn = new Random();
        int range = max - min + 1;
        return rn.nextInt(range) + min;
    }

}
