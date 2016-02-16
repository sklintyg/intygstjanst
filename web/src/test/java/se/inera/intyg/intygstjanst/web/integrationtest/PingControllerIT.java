package se.inera.intyg.intygstjanst.web.integrationtest;

import static com.jayway.restassured.RestAssured.given;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

/**
 * Created by eriklupander on 2016-02-15.
 */
public class PingControllerIT extends BaseIntegrationTest {

    @Test
    public void testPostDebugLog() {

        given().contentType(ContentType.JSON).
                expect().statusCode(200)
                .body(Matchers.containsString("OK"))
                .when().get("inera-certificate/health-check/ping");
    }
}
