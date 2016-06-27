/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.web.integrationtest.certificate;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.bind.JAXB;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.xml.XmlPath;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v2.RegisterCertificateType;

public class TransformToStatisticsIT extends BaseIntegrationTest{
    private STGroup templateGroup;
    private ST requestTemplate;
    private static final String BASE = "Envelope.Body.RegisterMedicalCertificateResponse.";
    private String intygsId = "1234589";
    private String personId1 = "192703104321";

    @SuppressWarnings("unused")
    @Autowired
    private IntygModuleRegistryImpl moduleRegistry;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/registermedicalcertificate/requests.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
        deleteIntyg(intygsId);
    }

    private void deleteIntyg(String id) {
        given().delete("inera-certificate/resources/certificate/" + id).then().statusCode(200);
    }
    @Test
    public void registerCertificateWorks() {

        requestTemplate.add("data", new IntygsData(intygsId, personId1));

        given().body(requestTemplate.render()).
                when().
                post("inera-certificate/register-certificate/v3.0").
                then().
                statusCode(200).
                rootPath(BASE).
                body("result.resultCode", is("OK"));

        XmlPath xml = given().contentType(ContentType.XML).accept(ContentType.XML).expect().statusCode(200).when().get("inera-certificate/resources/statisticsresource/"+intygsId).then().rootPath("RegisterCertificate")
        .body("intyg.intygs-id.extension", is(intygsId)).extract().body().xmlPath();

        RegisterCertificateType result = JAXB.unmarshal(new StringReader(xml.prettyPrint()), RegisterCertificateType.class);
        assertEquals(result.getIntyg().getPatient().getPersonId().getExtension(), personId1);
        assertEquals(intygsId, xml.get("RegisterCertificate.intyg.intygs-id.extension").toString());

    }

    @SuppressWarnings("unused")
    private static class IntygsData {
        public final String intygsId;
        public final String personId;

        public IntygsData(String intygsId, String personId) {
            this.intygsId = intygsId;
            this.personId = personId;
        }
    }
}
