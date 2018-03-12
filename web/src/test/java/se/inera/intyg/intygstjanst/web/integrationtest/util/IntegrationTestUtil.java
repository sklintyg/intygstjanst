/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.CertificateStateHolder;
import se.inera.intyg.schemas.contract.Personnummer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

public class IntegrationTestUtil {

    private static final String REGISTER_BASE = "Envelope.Body.RegisterCertificateResponse.";
    private static final String REGISTER_MEDICAL_BASE = "Envelope.Body.RegisterMedicalCertificateResponse.";
    private static final String CONSENT_BASE = "Envelope.Body.SetConsentResponse.";
    private static final String REVOKE_BASE = "Envelope.Body.RevokeCertificateResponse.";
    private static final String REVOKE_MEDICAL_BASE = "Envelope.Body.RevokeMedicalCertificateResponse.";
    private static final String SEND_BASE = "Envelope.Body.SendCertificateToRecipientResponse.";
    private static final String DEFAULT_FILE_PATH = "integrationtests/register/request_default.stg";
    private static final String REGISTER_TEMPLATE_WITH_DATES = "listActiveSickLeaves";

    public enum IntegrationTestCertificateType {
        LUSE,
        LUAENA,
        LUAEFS,
        LISJP
    }

    public static void registerCertificateFromTemplate(String intygsId, String personId, IntegrationTestCertificateType type) {
        String filePath = getFilePath(type);
        registerCertificateFromTemplate(intygsId, personId, filePath);
    }

    /**
     * Adds fromDate / toDate minus 2 days to plus 2 days from now() to the template.
     */
    public static void registerCertificateWithDateParameters(String intygsId, String personId, IntegrationTestCertificateType type) {
        String filePath = getFilePath(type);
        STGroup templateGroup = new STGroupFile(filePath);
        ST requestTemplateForConsent = templateGroup.getInstanceOf("requestParameterized");
        requestTemplateForConsent.add("intygId", intygsId);
        requestTemplateForConsent.add("personId", personId);

        applyToFromDatesToRequestTemplate(requestTemplateForConsent);

        executeRegisterCertificate(requestTemplateForConsent);
    }

    private static void executeRegisterCertificate(ST requestTemplateForConsent) {
        given().body(requestTemplateForConsent.render()).when().post("inera-certificate/register-certificate-se/v3.0").then().statusCode(200)
                .rootPath(REGISTER_BASE).body("result.resultCode", is("OK"));
    }

    private static void applyToFromDatesToRequestTemplate(ST requestTemplate) {
        requestTemplate.add("fromDate", LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        requestTemplate.add("toDate", LocalDate.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    private static String getFilePath(IntegrationTestCertificateType type) {
        String baseUrl = "integrationtests/register/";
        switch (type) {
        case LISJP:
            return baseUrl + "request_lisjp.stg";
        case LUSE:
            return baseUrl + "request_luse.stg";
        case LUAEFS:
            return baseUrl + "request_luaefs.stg";
        case LUAENA:
            return baseUrl + "request_luaena.stg";
        default:
            return DEFAULT_FILE_PATH;
        }
    }

    public static void registerCertificateFromTemplate(String intygsId, String personId) {
        registerCertificateFromTemplate(intygsId, personId, DEFAULT_FILE_PATH);
    }

    private static void registerCertificateFromTemplate(String intygsId, String personId, String filePath) {
        STGroup templateGroup = new STGroupFile(filePath);
        ST requestTemplateForConsent = templateGroup.getInstanceOf("request");
        requestTemplateForConsent.add("data", new IntygsData(intygsId, personId));
        executeRegisterCertificate(requestTemplateForConsent);
    }

    public static void addConsent(String personId) {
        setConsent(personId, true);
    }

    public static void revokeConsent(String personId) {
        setConsent(personId, false);
    }

    public static void deleteIntyg(String id) {
        given().delete("inera-certificate/resources/certificate/" + id).then().statusCode(200);
    }

    public static void deleteCertificatesForCitizen(String personId) {
        given().delete("inera-certificate/resources/certificate/citizen/" + personId).then().statusCode(200);
    }

    public static void deleteCertificatesForUnit(String careUnitId) {
        given().delete("inera-certificate/resources/certificate/unit/" + careUnitId).then().statusCode(200);
    }

    public static void givenIntyg(String intygId, String intygTyp, String personId, boolean deletedByCareGiver) {
        given().contentType("application/json;charset=utf-8").body(certificate(intygId, intygTyp, personId, deletedByCareGiver))
                .post("inera-certificate/resources/certificate/").then().statusCode(200);
    }

    public static void revokeCertificate(String intygsId, String personId) {
        ST requestTemplateForRevoke = getRequestTemplate("revokecertificate/requests.stg");
        requestTemplateForRevoke.add("data", new IntygsData(intygsId, personId));
        given().body(requestTemplateForRevoke.render()).when().post("inera-certificate/revoke-certificate-rivta/v2.0").then().statusCode(200)
                .rootPath(REVOKE_BASE).body("result.resultCode", is("OK"));

    }

    public static void revokeMedicalCertificate(String intygsId, String personId, String meddelande) {
        ST requestTemplateForRevoke = getRequestTemplate("revokemedicalcertificate/requests.stg");
        requestTemplateForRevoke.add("intygsId", intygsId);
        requestTemplateForRevoke.add("personId", personId);
        requestTemplateForRevoke.add("meddelande", meddelande);
        given().body(requestTemplateForRevoke.render()).when().post("inera-certificate/revoke-certificate/v1.0").then().statusCode(200)
                .rootPath(REVOKE_MEDICAL_BASE).body("result.resultCode", is("OK"));
    }

    public static void registerMedicalCertificate(String intygsId, String personId) {
        registerMedicalCertificate(intygsId, personId, "request");
    }

    public static void registerMedicalCertificate(String intygsId, String personId, String template) {
        ST requestTemplateForRegister = getRequestTemplate("registermedicalcertificate/requests.stg", template);
        requestTemplateForRegister.add("intygId", intygsId);
        requestTemplateForRegister.add("personId", personId);
        if (REGISTER_TEMPLATE_WITH_DATES.equals(template)) {
            applyToFromDatesToRequestTemplate(requestTemplateForRegister);
        }
        given().body(requestTemplateForRegister.render()).when().post("inera-certificate/register-certificate/v3.0").then().statusCode(200)
                .rootPath(REGISTER_MEDICAL_BASE).body("result.resultCode", is("OK"));
    }

    public static void sendCertificateToRecipient(String intygsId, String personId) {
        ST requestTemplateRecipient = getRequestTemplate("sendcertificatetorecipient/requests.stg");
        requestTemplateRecipient.add("data", new IntygsData(intygsId, personId));
        requestTemplateRecipient.add("mottagare", "FKASSA");

        given().body(requestTemplateRecipient.render()).when().post("inera-certificate/send-certificate-to-recipient/v2.0").then().statusCode(200)
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
    private static class ConsentData {
        public final String personId;
        public final Boolean consent;

        public ConsentData(String personId, Boolean consent) {
            this.personId = personId;
            this.consent = consent;
        }
    }

    private static CertificateHolder certificate(String intygId, String intygTyp, String personId, boolean deletedByCareGiver) {
        CertificateHolder certificate = new CertificateHolder();
        certificate.setId(intygId);
        certificate.setType(intygTyp);
        certificate.setSignedDate(LocalDateTime.now());
        certificate.setCareGiverId("CareGiverId");
        certificate.setCareUnitId("CareUnitId");
        certificate.setCareUnitName("CareUnitName");
        certificate.setSigningDoctorName("Singing Doctor");
        certificate.setCivicRegistrationNumber(createPnr(personId));
        certificate.setCertificateStates(Arrays.asList(new CertificateStateHolder("HSVARD", CertificateState.RECEIVED, LocalDateTime.now())));
        certificate.setDeletedByCareGiver(deletedByCareGiver);
        return certificate;
    }

    private static void setConsent(String personId, Boolean consent) {
        ST registerTemplateForConsent = getRequestTemplate("setconsent/requests.stg");
        registerTemplateForConsent.add("data", new ConsentData(personId, consent));

        given().body(registerTemplateForConsent.render()).when().post("inera-certificate/set-consent/v1.0").then().statusCode(200)
                .rootPath(CONSENT_BASE).body("result.resultCode", is("OK"));
    }

    private static ST getRequestTemplate(String path) {
        return getRequestTemplate(path, "request");
    }

    private static ST getRequestTemplate(String path, String instance) {
        String base = "integrationtests/";
        STGroup templateGroup = new STGroupFile(base + path);
        ST registerTemplateForConsent = templateGroup.getInstanceOf(instance);
        return registerTemplateForConsent;
    }

    private static Personnummer createPnr(String pnr) {
        return Personnummer.createValidatedPersonnummer(pnr)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }

}
