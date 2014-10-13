package se.inera.certificate.tools.anonymisering

import groovy.json.*

import org.apache.commons.io.FileUtils
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.core.io.ClassPathResource

import se.inera.certificate.tools.anonymisering.AnonymiseraDatum;
import se.inera.certificate.tools.anonymisering.AnonymiseraHsaId;
import se.inera.certificate.tools.anonymisering.AnonymiseraJson;

class AnonymiseraJsonTest {

    AnonymiseraHsaId anonymiseraHsaId = [anonymisera:{"SE1010"}] as AnonymiseraHsaId
    AnonymiseraDatum anonymiseraDatum = new AnonymiseraDatum()
    AnonymiseraJson anonymiseraJson = new AnonymiseraJson(anonymiseraHsaId, anonymiseraDatum)
    
    AnonymiseraJsonTest() {
        anonymiseraDatum.random = [nextInt: {(AnonymiseraDatum.DATE_RANGE/2)+1}] as Random
    }
    
    @Test
    void testaAnonymiseringAvMaximaltIntyg() {
        String json = FileUtils.readFileToString(new ClassPathResource("/fk7263_L_template.json").getFile(), "UTF-8")
        String expected = FileUtils.readFileToString(new ClassPathResource("/fk7263_L_anonymized.json").getFile(), "UTF-8")
        String actual = anonymiseraJson.anonymiseraIntygsJson(json, "10101010-1010")
        JSONAssert.assertEquals(expected, actual, true);
    }
    
}
