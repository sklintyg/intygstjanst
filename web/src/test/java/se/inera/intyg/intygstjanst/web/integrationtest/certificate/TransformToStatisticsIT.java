/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.UUID;

import javax.xml.bind.JAXB;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Charsets;
import com.helger.schematron.svrl.SVRLHelper;
import com.helger.schematron.xslt.SchematronResourceSCH;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.xml.XmlPath;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;

public class TransformToStatisticsIT extends BaseIntegrationTest{

    private String intygsId = UUID.randomUUID().toString();
    private String personId1 = "192703104321";

    @SuppressWarnings("unused")
    @Autowired
    private IntygModuleRegistryImpl moduleRegistry;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        IntegrationTestUtil.deleteIntyg(intygsId);
    }

    @Test
    public void registerCertificateWorks() throws Exception {
        IntegrationTestUtil.registerMedicalCertificate(intygsId, personId1);

        XmlPath xml = given().contentType(ContentType.XML).pathParams("id", intygsId, "action", "created").accept(ContentType.XML).expect()
                .statusCode(200).when()
                .get("inera-certificate/resources/statisticsresource/{id}/{action}").then().rootPath("RegisterCertificate")
                .body("intyg.intygs-id.extension", is(intygsId)).extract().body().xmlPath();

        if (!validateFk7263OutputSchematron(xml.prettyPrint())) {
            fail();
        }

        RegisterCertificateType result = JAXB.unmarshal(new StringReader(xml.prettyPrint()), RegisterCertificateType.class);
        assertEquals(result.getIntyg().getPatient().getPersonId().getExtension(), personId1);
        assertEquals(intygsId, xml.get("RegisterCertificate.intyg.intygs-id.extension").toString());
    }

    private boolean validateFk7263OutputSchematron(String xml) throws Exception {
        SchematronResourceSCH schematronResource = SchematronResourceSCH.fromClassPath("fk7263sit.sch");
        if (!schematronResource.isValidSchematron()) {
            throw new IllegalArgumentException("Invalid Schematron!");
        }
        SchematronOutputType result = schematronResource
                .applySchematronValidationToSVRL((new StreamSource(new ByteArrayInputStream(xml.getBytes(Charsets.UTF_8)))));
        return SVRLHelper.getAllFailedAssertions(result).size() == 0;
    }
}
