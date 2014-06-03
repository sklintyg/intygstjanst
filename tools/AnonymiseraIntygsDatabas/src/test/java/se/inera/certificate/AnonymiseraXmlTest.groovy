package se.inera.certificate

import static org.custommonkey.xmlunit.DifferenceConstants.NAMESPACE_PREFIX_ID;

import groovy.json.*

import org.w3c.dom.Node
import org.apache.commons.io.FileUtils
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.Difference
import org.custommonkey.xmlunit.DifferenceListener
import org.custommonkey.xmlunit.XMLAssert
import org.custommonkey.xmlunit.XMLUnit
import org.junit.Test
import org.springframework.core.io.ClassPathResource

class AnonymiseraXmlTest {

    AnonymiseraHsaId anonymiseraHsaId = [anonymisera:{"SE1010"}] as AnonymiseraHsaId
    AnonymiseraXml anonymiseraXml = new AnonymiseraXml(anonymiseraHsaId)

    @Test
    void testaAnonymiseringAvMaximaltIntyg() {
        String xml = FileUtils.readFileToString(new ClassPathResource("/fk7263_L_template.xml").getFile(), "UTF-8")

        String expected = FileUtils.readFileToString(new ClassPathResource("/fk7263_L_anonymized.xml").getFile(), "UTF-8")
        String actual = anonymiseraXml.anonymiseraIntygsXml(xml, "10101010-1010")
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);
        Diff diff = new Diff(expected, actual);
        diff.overrideDifferenceListener(new NamespacePrefixNameIgnoringListener());

        assert diff.identical(), diff.toString()
    }

    private class NamespacePrefixNameIgnoringListener implements DifferenceListener {
        public int differenceFound(Difference difference) {
            if (NAMESPACE_PREFIX_ID == difference.getId()) {
                // differences in namespace prefix IDs are ok (eg. 'ns1' vs 'ns2'), as long as the namespace URI is the
                // same
                return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            } else {
                return RETURN_ACCEPT_DIFFERENCE;
            }
        }
        public void skippedComparison(Node control, Node test) {
        }
    }

}
