package se.inera.certificate.tools.anonymisering

import org.junit.Test

import se.inera.certificate.tools.anonymisering.AnonymizeString;

class AnonymizeStringTest {

    @Test
    void anonymiseraBokstäverOchSiffror() {
        String s = "Text som innehåller specialtecken {}'\"() och 0123456789."
        String expected = "xxxx xxx xxxxxxxxxx xxxxxxxxxxxxx {}'\"() xxx 9999999999x"
        assert AnonymizeString.anonymize(s) == expected
    }

}
