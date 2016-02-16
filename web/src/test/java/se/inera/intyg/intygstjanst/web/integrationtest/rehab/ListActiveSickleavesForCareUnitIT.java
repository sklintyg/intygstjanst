package se.inera.intyg.intygstjanst.web.integrationtest.rehab;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import com.jayway.restassured.internal.path.xml.NodeImpl;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.xml.XmlPath;

/**
 * Created by eriklupander on 2016-02-16.
 */
public class ListActiveSickleavesForCareUnitIT extends BaseIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ListActiveSickleavesForCareUnitIT.class);

    private static final String REQUEST = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:riv:itintegration:registry:1\" xmlns:urn1=\"urn:riv:clinicalprocess:healthcond:rehabilitation:ListActiveSickLeavesForCareUnitResponder:1\" xmlns:urn2=\"urn:riv:clinicalprocess:healthcond:certificate:types:2\">\n" +
            "   <soapenv:Header>\n" +
            "      <urn:LogicalAddress>1</urn:LogicalAddress>\n" +
            "   </soapenv:Header>\n" +
            "   <soapenv:Body>\n" +
            "      <urn1:ListActiveSickLeavesForCareUnit>\n" +
            "         <urn1:enhets-id>\n" +
            "            <urn2:root>1.2.752.129.2.1.4.1</urn2:root>\n" +
            "            <urn2:extension>{{careUnitHsaId}}</urn2:extension>\n" +
            "         </urn1:enhets-id>\n" +
            "         <!--You may enter ANY elements at this point-->\n" +
            "      </urn1:ListActiveSickLeavesForCareUnit>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    @Test
    public void testReadIntygsData() {
        given().contentType(ContentType.XML)
                .with().body(REQUEST.replace("{{careUnitHsaId}}", "centrum-vast"))
                .expect().statusCode(200)
                .body(new OkResultMatcher())
                .when().post("inera-certificate/list-active-sick-leaves-for-care-unit/v1.0");

        // Matchers.hasXPath("/soap:Envelope/soap:Body/ns2:ListActiveSickLeavesForCareUnitResponse/ns2:resultCode>OK</ns2:resultCode"))
    }

    private class OkResultMatcher extends BaseMatcher<String> {

        private String gpath = "Envelope.Body.ListActiveSickLeavesForCareUnitResponse.resultCode";

        @Override
        public boolean matches(Object o) {
            XmlPath xmlPath = XmlPath.given((String) o);
            String o1 = xmlPath.get(gpath);
            return o1.equals("OK");
        }

        @Override
        public void describeTo(Description description) {

        }
    }
}
