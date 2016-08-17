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
package se.inera.intyg.intygstjanst.web.integrationtest.util;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class IntegrationTestUtil {

    private static final String REGISTER_BASE = "Envelope.Body.RegisterCertificateResponse.";
    private static final String CONSENT_BASE = "Envelope.Body.SetConsentResponse.";
    private static final String REVOKE_BASE = "Envelope.Body.RevokeCertificateResponse.";
    private static final String SEND_BASE = "Envelope.Body.SendCertificateToRecipientResponse.";
    private static final String DEFAULT_FILE_PATH = "integrationtests/register/request_default.stg";
    public enum IntegrationTestCertificateType{
        LUSE, LUAENA, LUAEFS, LISU
    }

    public static void registerCertificate(String intygsId, String personId, IntegrationTestCertificateType type) {
        String filePath = getFilePath(type);
        registerCertificate(intygsId, personId, filePath);
    }

    private static String getFilePath(IntegrationTestCertificateType type) {
        String baseUrl = "integrationtests/register/";
        switch(type){
        case LISU:
            return baseUrl+"request_lisu.stg";
        case LUSE:
            return baseUrl+"request_luse.stg";
        case LUAEFS:
            return baseUrl+"request_luaefs.stg";
        case LUAENA:
            return baseUrl+"request_luaena.stg";
            default:
                return DEFAULT_FILE_PATH;
        }
    }


    public static void registerCertificate(String intygsId, String personId) {
        registerCertificate(intygsId, personId, DEFAULT_FILE_PATH);
    }

    private static void registerCertificate(String intygsId, String personId, String filePath){
        STGroup templateGroup = new STGroupFile(filePath);
        ST requestTemplateForConsent = templateGroup.getInstanceOf("request");
        requestTemplateForConsent.add("data", new IntygsData(intygsId, personId));

        given().body(requestTemplateForConsent.render()).
                when().
                post("inera-certificate/register-certificate-se/v2.0").
                then().
                statusCode(200).
                rootPath(REGISTER_BASE).
                body("result.resultCode", is("OK"));
    }

    public static void addConsent(String personId){
        setConsent(personId, true);
    }

    public static void revokeConsent(String personId){
        setConsent(personId, false);
    }

    private static void setConsent(String personId, Boolean consent) {
        ST registerTemplateForConsent = getRequestTemplate("setconsent/requests.stg");
        registerTemplateForConsent.add("data", new ConsentData(personId, consent));

        given().body(registerTemplateForConsent.render()).when().post("inera-certificate/set-consent/v1.0").then().statusCode(200)
                .rootPath(CONSENT_BASE).body("result.resultCode", is("OK"));
    }

    private static ST getRequestTemplate(String path) {
        String base = "integrationtests/";
        STGroup templateGroup = new STGroupFile(base+path);
        ST registerTemplateForConsent = templateGroup.getInstanceOf("request");
        return registerTemplateForConsent;
    }

    public static void deleteIntyg(String id) {
        given().delete("inera-certificate/resources/certificate/" + id).then().statusCode(200);
    }

    public static void revokeCertificate(String intygsId, String personId) {
        ST requestTemplateForRevoke = getRequestTemplate("revokecertificate/requests.stg");
        requestTemplateForRevoke.add("data", new IntygsData(intygsId, personId));
        given().body(requestTemplateForRevoke.render()).
        when().
        post("inera-certificate/revoke-certificate-rivta/v1.0").
        then().
        statusCode(200).
        rootPath(REVOKE_BASE).
        body("result.resultCode", is("OK"));

    }

    public static void sendCertificateToRecipient(String intygsId, String personId) {
        ST requestTemplateRecipient  = getRequestTemplate("sendcertificatetorecipient/requests.stg");
        requestTemplateRecipient.add("data", new IntygsData(intygsId, personId));

        given().body(requestTemplateRecipient.render()).when().post("inera-certificate/send-certificate-to-recipient/v1.0").then().statusCode(200)
                .rootPath(SEND_BASE).body("result.resultCode", is("OK"));
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

    @SuppressWarnings("unused")
    private static class ConsentData{
        public final String personId;
        public final Boolean consent;

        public ConsentData(String personId, Boolean consent) {
            this.personId = personId;
            this.consent = consent;
        }
    }

}
