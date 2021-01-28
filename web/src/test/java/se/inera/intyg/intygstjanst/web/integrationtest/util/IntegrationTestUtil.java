/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.CertificateStateHolder;
import se.inera.intyg.schemas.contract.Personnummer;

public class IntegrationTestUtil {

    private static final String REGISTER_BASE = "Envelope.Body.RegisterCertificateResponse.";
    private static final String REGISTER_MEDICAL_BASE = "Envelope.Body.RegisterMedicalCertificateResponse.";
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

    public static void registerCertificateFromTemplate(String intygsId, String versionsId, String personId,
        IntegrationTestCertificateType type) {
        String filePath = getFilePath(type);
        registerCertificateFromTemplate(intygsId, versionsId, personId, filePath);
    }

    public static void registerCertificateWithDateParameters(String intygsId, String personId, IntegrationTestCertificateType type,
        int fromDaysRelativeToNow, int toDaysRelativeToNow) {
        String filePath = getFilePath(type);
        STGroup templateGroup = new STGroupFile(filePath);
        ST requestTemplate = templateGroup.getInstanceOf("requestParameterized");
        requestTemplate.add("intygId", intygsId);
        requestTemplate.add("personId", personId);

        applyToFromDatesToRequestTemplate(requestTemplate, fromDaysRelativeToNow, toDaysRelativeToNow);

        executeRegisterCertificate(requestTemplate);
    }

    private static void executeRegisterCertificate(ST requestTemplate) {
        given().body(requestTemplate.render()).when().post("inera-certificate/register-certificate-se/v3.0").then().statusCode(200)
            .rootPath(REGISTER_BASE).body("result.resultCode", is("OK"));
    }

    private static void applyToFromDatesToRequestTemplate(ST requestTemplate, int fromDaysRelativeToNow, int toDaysRelativeToNow) {
        requestTemplate.add("fromDate", LocalDate.now().plusDays(fromDaysRelativeToNow).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        requestTemplate.add("toDate", LocalDate.now().plusDays(toDaysRelativeToNow).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
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

    public static void registerCertificateFromTemplate(String intygsId, String versionsId, String personId) {
        registerCertificateFromTemplate(intygsId, versionsId, personId, DEFAULT_FILE_PATH);
    }

    private static void registerCertificateFromTemplate(String intygsId, String versionsId, String personId, String filePath) {
        STGroup templateGroup = new STGroupFile(filePath);
        ST requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("data", new RegisterIntygsData(intygsId, versionsId, personId));
        executeRegisterCertificate(requestTemplate);
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

    public static void givenIntyg(String intygId, String intygTyp, String intygTypeVersion, String personId, boolean deletedByCareGiver) {
        given().contentType("application/json;charset=utf-8")
            .body(certificate(intygId, intygTyp, intygTypeVersion, personId, deletedByCareGiver))
            .post("inera-certificate/resources/certificate/").then().statusCode(200);
    }

    public static void revokeCertificate(String intygsId, String personId) {
        ST requestTemplateForRevoke = getRequestTemplate("revokecertificate/requests.stg");
        requestTemplateForRevoke.add("data", new RevokeIntygsData(intygsId, personId));
        given().body(requestTemplateForRevoke.render()).when().post("inera-certificate/revoke-certificate-rivta/v2.0").then()
            .statusCode(200)
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
            applyToFromDatesToRequestTemplate(requestTemplateForRegister, -2, 2);
        }
        given().body(requestTemplateForRegister.render()).when().post("inera-certificate/register-certificate/v3.0").then().statusCode(200)
            .rootPath(REGISTER_MEDICAL_BASE).body("result.resultCode", is("OK"));
    }

    public static void sendCertificateToRecipient(String intygsId, String personId) {
        ST requestTemplateRecipient = getRequestTemplate("sendcertificatetorecipient/requests.stg");
        requestTemplateRecipient.add("data", new SendIntygsData(intygsId, personId));
        requestTemplateRecipient.add("mottagare", "FKASSA");

        given().body(requestTemplateRecipient.render()).when().post("inera-certificate/send-certificate-to-recipient/v2.0").then()
            .statusCode(200)
            .rootPath(SEND_BASE).body("result.resultCode", is("OK"));
    }

    private static CertificateHolder certificate(String intygId, String intygTyp, String intygTypeVersion, String personId,
        boolean deletedByCareGiver) {
        CertificateHolder certificate = new CertificateHolder();
        certificate.setId(intygId);
        certificate.setType(intygTyp);
        certificate.setTypeVersion(intygTypeVersion);
        certificate.setSignedDate(LocalDateTime.now());
        certificate.setCareGiverId("CareGiverId");
        certificate.setCareUnitId("CareUnitId");
        certificate.setCareUnitName("CareUnitName");
        certificate.setSigningDoctorName("Singing Doctor");
        certificate.setCivicRegistrationNumber(createPnr(personId));
        certificate
            .setCertificateStates(Arrays.asList(new CertificateStateHolder("HSVARD", CertificateState.RECEIVED, LocalDateTime.now())));
        certificate.setDeletedByCareGiver(deletedByCareGiver);
        return certificate;
    }

    private static ST getRequestTemplate(String path) {
        return getRequestTemplate(path, "request");
    }

    private static ST getRequestTemplate(String path, String instance) {
        String base = "integrationtests/";
        STGroup templateGroup = new STGroupFile(base + path);
        ST requestTemplate = templateGroup.getInstanceOf(instance);
        return requestTemplate;
    }

    private static Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }

    @SuppressWarnings("unused")
    protected static class RevokeIntygsData {

        public final String intygsId;
        public final String personId;

        public RevokeIntygsData(String intygsId, String personId) {
            this.intygsId = intygsId;
            this.personId = personId;
        }
    }

    @SuppressWarnings("unused")
    protected static class RegisterIntygsData {

        public final String intygsId;
        public final String intygsVersion;
        public final String personId;

        public RegisterIntygsData(final String intygsId, final String intygsVersion, final String personId) {
            this.intygsId = intygsId;
            this.intygsVersion = intygsVersion;
            this.personId = personId;
        }
    }

    @SuppressWarnings("unused")
    protected static class SendIntygsData {

        public final String intygsId;
        public final String personId;

        SendIntygsData(String intygsId, String personId) {
            this.intygsId = intygsId;
            this.personId = personId;
        }
    }
}
