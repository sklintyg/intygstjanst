package se.inera.certificate

import groovy.json.*

import org.apache.commons.io.FileUtils
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.core.io.ClassPathResource

class AnonymiseraJsonTest {

    AnonymiseraPersonId anonymiseraPersonId = [anonymisera:{"10101010-1010"}] as AnonymiseraPersonId
    AnonymiseraHsaId anonymiseraHsaId = [anonymisera:{"SE1010"}] as AnonymiseraHsaId
    AnonymiseraJson anonymiseraJson = new AnonymiseraJson(anonymiseraPersonId, anonymiseraHsaId)

    @Test
    void testaAnonymiseringAvMaximaltIntyg() {
        String json = FileUtils.readFileToString(new ClassPathResource("/fk7263_L_template.json").getFile(), "UTF-8")
        String expected = FileUtils.readFileToString(new ClassPathResource("/fk7263_L_anonymized.json").getFile(), "UTF-8")
        String actual = anonymiseraJson.anonymiseraIntygsJson(json)
        JSONAssert.assertEquals(expected, actual, true);
    }
    
}
