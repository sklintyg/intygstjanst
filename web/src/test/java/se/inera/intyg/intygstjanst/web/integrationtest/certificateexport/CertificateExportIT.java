/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.integrationtest.certificateexport;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil.IntegrationTestCertificateType.LISJP;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateExportPageDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateTextDTO;

public class CertificateExportIT {

    private static final String INTERNAL_BASE_URI = System.getProperty("integration.tests.actuatorUrl", "http://localhost:8081");
    private static final String CERTIFICATE_TEXTS_URL = INTERNAL_BASE_URI + "/inera-certificate/internalapi/v1/certificatetexts";
    private static final String CERTIFICATES_URL = INTERNAL_BASE_URI + "/inera-certificate/internalapi/v1/certificates/";
    private static final String ERASE_CERTIFICATES_URL = INTERNAL_BASE_URI + "/inera-certificate/internalapi/v1/certificates/";

    private static final String PERSON_ID = "191212121212";
    private static final String LISJP_VERSION = "1.3";
    private static final String UNIT_ID = "UNIT_ID";
    private static final String CARE_PROVIDER_ID = "SE2321000016-39KJ";
    private static final String OTHER_CARE_PROVIDER_ID = "SE2321000025-39AB";

    private static final int PAGE1 = 0;
    private static final int PAGE2 = 1;
    private static final int PAGE_SIZE = 3;
    private static final int CERTIFICATES_COUNT = 4;

    private static final TypeRef<List<CertificateTextDTO>> CERTIFICATE_TEXTS = new TypeRef<>() { };

    private ST requestTemplate;
    private final List<String> createdCertificates = new ArrayList<>();

    @BeforeEach
    public void setupBase() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl", "http://localhost:8080/");
    }

    @AfterEach
    public void cleanupBase() {
        RestAssured.reset();
    }

    @Nested
    class CertificateTexts {

        @Test
        public void shouldSuccessfullyFetchCertificateTexts() {
            final var texts = given()
                .when().get(CERTIFICATE_TEXTS_URL)
                .then().statusCode(HttpStatus.OK.value())
                .extract().response().getBody().as(CERTIFICATE_TEXTS);

            assertAll(
                () -> assertTrue(texts.size() >= 28)
            );
        }
    }

    @Nested
    class CertificateExport {

        @BeforeEach
        public void setup() {
            setupCertificatesForExport();
            setupRevoked("CertificateExportIT-2");
        }

        @AfterEach
        public void cleanUp() {
            deleteCertificates();
        }

        @Test
        public void shouldHaveCorrectDataOnFirstCertificatePage() {
            final var page = given()
                .when().get(CERTIFICATES_URL + CARE_PROVIDER_ID + "/?page=" + PAGE1 + "&size=" + PAGE_SIZE)
                .then().statusCode(HttpStatus.OK.value())
                .extract().response().getBody().as(CertificateExportPageDTO.class);

            assertAll(
                () -> assertEquals(CARE_PROVIDER_ID, page.getCareProviderId()),
                () -> assertEquals(PAGE_SIZE, page.getCount()),
                () -> assertEquals(PAGE1, page.getPage()),
                () -> assertEquals(CERTIFICATES_COUNT, page.getTotal()),
                () -> assertEquals(1, page.getTotalRevoked())
            );
        }

        @Test
        public void shouldHaveCorrectCertificatesOnFirstCertificatePage() {
            final var page = given()
                .when().get(CERTIFICATES_URL + CARE_PROVIDER_ID + "/?page=" + PAGE1 + "&size=" + PAGE_SIZE)
                .then().statusCode(HttpStatus.OK.value())
                .extract().response().getBody().as(CertificateExportPageDTO.class);

            assertAll(
                () -> assertEquals(PAGE_SIZE, page.getCertificateXmls().size()),
                () -> assertFalse(page.getCertificateXmls().get(0).isRevoked()),
                () -> assertFalse(page.getCertificateXmls().get(1).isRevoked()),
                () -> assertTrue(page.getCertificateXmls().get(2).isRevoked())
            );
        }

        @Test
        public void shouldHaveCorrectDataOnSecondCertificatePage() {
            final var page = given()
                .when().get(CERTIFICATES_URL + CARE_PROVIDER_ID + "/?page=" + PAGE2 + "&size=" + PAGE_SIZE)
                .then().statusCode(HttpStatus.OK.value())
                .extract().response().getBody().as(CertificateExportPageDTO.class);

            assertAll(
                () -> assertEquals(CARE_PROVIDER_ID, page.getCareProviderId()),
                () -> assertEquals(1, page.getCount()),
                () -> assertEquals(PAGE2, page.getPage()),
                () -> assertEquals(CERTIFICATES_COUNT, page.getTotal()),
                () -> assertEquals(1, page.getTotalRevoked())
            );
        }

        @Test
        public void shouldHaveCorrectCertificatesOnSecondCertificatePage() {
            final var page = given()
                .when().get(CERTIFICATES_URL + CARE_PROVIDER_ID + "/?page=" + PAGE2 + "&size=" + PAGE_SIZE)
                .then().statusCode(HttpStatus.OK.value())
                .extract().response().getBody().as(CertificateExportPageDTO.class);

            assertAll(
                () -> assertEquals(1, page.getCertificateXmls().size()),
                () -> assertFalse(page.getCertificateXmls().get(0).isRevoked())
            );
        }
    }

    @Nested
    class EraseCertificates {

        @BeforeEach
        public void setup() {
            createdCertificates.clear();
            setupCertificatesForErase(CARE_PROVIDER_ID);
        }

        @AfterEach
        public void cleanup() {
            deleteDataForCareProviders();
        }

        @Test
        public void shouldEraseCertificates() {
            assertEquals(CERTIFICATES_COUNT, getCount("/certificateCount", CARE_PROVIDER_ID));
            eraseCertificates(CARE_PROVIDER_ID);
            assertEquals(0, getCount("/certificateCount", CARE_PROVIDER_ID));
        }

        @Test
        public void shouldEraseSjukfallCertificates() {
            assertEquals(CERTIFICATES_COUNT, getCount("/sjukfallCertificateCount", CARE_PROVIDER_ID));
            eraseCertificates(CARE_PROVIDER_ID);
            assertEquals(0, getCount("/sjukfallCertificateCount", CARE_PROVIDER_ID));
        }

        @Test
        public void shouldEraseMessages() {
            assertEquals(CERTIFICATES_COUNT * 2, getCount("/messageCount", CARE_PROVIDER_ID));
            eraseCertificates(CARE_PROVIDER_ID);
            assertEquals(0, getCount("/messageCount", CARE_PROVIDER_ID));
        }

        @Test
        public void shouldEraseRelations() {
            assertEquals(CERTIFICATES_COUNT - 1, getCount("/relationCount", CARE_PROVIDER_ID));
            eraseCertificates(CARE_PROVIDER_ID);
            assertEquals(0, getCount("/relationCount", CARE_PROVIDER_ID));
        }

        @Test
        public void shouldEraseApprovedReceivers() {
            assertEquals(CERTIFICATES_COUNT * 2, getCount("/approvedReceiverCount", CARE_PROVIDER_ID));
            eraseCertificates(CARE_PROVIDER_ID);
            assertEquals(0, getCount("/approvedReceiverCount", CARE_PROVIDER_ID));
        }

        @Test
        public void shouldNotEraseDataForOtherCareProvidrers() {
            setupCertificatesForErase(OTHER_CARE_PROVIDER_ID);

            eraseCertificates(CARE_PROVIDER_ID);

            assertAll(
                () -> assertEquals(CERTIFICATES_COUNT, getCount("/certificateCount", OTHER_CARE_PROVIDER_ID)),
                () -> assertEquals(CERTIFICATES_COUNT, getCount("/sjukfallCertificateCount", OTHER_CARE_PROVIDER_ID)),
                () -> assertEquals(CERTIFICATES_COUNT * 2, getCount("/messageCount", OTHER_CARE_PROVIDER_ID)),
                () -> assertEquals(CERTIFICATES_COUNT - 1, getCount("/relationCount", OTHER_CARE_PROVIDER_ID)),
                () -> assertEquals(CERTIFICATES_COUNT * 2, getCount("/approvedReceiverCount", OTHER_CARE_PROVIDER_ID))
            );
        }

        @Test
        public void shouldReturnOkResponseWhenNoCertificatesFound() {
            eraseCertificates(OTHER_CARE_PROVIDER_ID);
        }
    }

    private void setupCertificatesForExport() {
        for (int i = 0; i < CERTIFICATES_COUNT; i++) {
            IntegrationTestUtil.registerCertificateFromTemplate("CertificateExportIT-" + i, LISJP_VERSION, PERSON_ID, LISJP);
        }
    }

    private void setupRevoked(String certificateId) {
        IntegrationTestUtil.revokeCertificate(certificateId, PERSON_ID);
    }

    private void deleteCertificates() {
        for (int i = 0; i < CERTIFICATES_COUNT; i++) {
            IntegrationTestUtil.deleteIntyg("CertificateExportIT-" + i);
        }
    }

    private void eraseCertificates(String careProviderId) {
        given()
            .when().delete(ERASE_CERTIFICATES_URL + careProviderId)
            .then().statusCode(204);
    }

    private void setupCertificatesForErase(String careProviderId) {
        for (int i = 0; i < CERTIFICATES_COUNT; i++) {
            final var relationId = createdCertificates.size() % CERTIFICATES_COUNT != 0 ?
                createdCertificates.get(createdCertificates.size() - 1) : null;
            final var certificateId = IntegrationTestUtil.registerCertificateForErase(LISJP, careProviderId, relationId);
            createdCertificates.add(certificateId);
            setupMessagesToCare(certificateId);
            setupMessagesToRecipient(certificateId);
            setupApprovedReceivers(certificateId);

            if (createdCertificates.size() % CERTIFICATES_COUNT - 1 == 0) {
                setupRevoked(certificateId);
            }
        }
    }

    private void deleteDataForCareProviders() {
        given().contentType(ContentType.JSON).body(createdCertificates)
            .when().delete("inera-certificate/resources/certificate/deleteCertificates")
            .then().statusCode(200);
    }

    private int getCount(String path, String careProviderId) {
        return given()
            .when().get("inera-certificate/resources/certificate/" + careProviderId + path)
            .then().statusCode(200).extract().body().as(Integer.class);
    }

    private void setupMessagesToCare(String certificateId) {
        final var templateGroup = new STGroupFile("integrationtests/arende/request_care.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("data", new ArendeData(certificateId, "KOMPL"));

        given().contentType(ContentType.XML).body(requestTemplate.render())
            .when().post("inera-certificate/send-message-to-care/v2.0")
            .then().statusCode(200).rootPath("Envelope.Body.SendMessageToCareResponse.")
            .body("result.resultCode", is("OK"));
    }

    private void setupMessagesToRecipient(String certificateId) {
        final var templateGroup = new STGroupFile("integrationtests/arende/request_recipient.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("data", new ArendeData(certificateId, "KOMPL"));

        given().body(requestTemplate.render())
            .when().post("inera-certificate/send-message-to-recipient/v2.0")
            .then().statusCode(200).rootPath("Envelope.Body.SendMessageToRecipientResponse.")
            .body("result.resultCode", is("OK"));
    }

    private void setupApprovedReceivers(String certificateId) {
        final var templateGroup = new STGroupFile("integrationtests/registerapprovedreceivers/requests.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("intygsId", certificateId);
        requestTemplate.add("intygsTyp", "LISJP");
        requestTemplate.add("mottagare", "FKASSA");

        given().body(requestTemplate.render())
            .when().post("inera-certificate/register-approved-receivers/v1.0")
            .then().statusCode(200).rootPath("Envelope.Body.RegisterApprovedReceiversResponse.")
            .body("result.resultCode", is("OK"));
    }

    private static class ArendeData {
        public final String intygsId;
        public final String arende;
        public final String personId;
        public final String enhetsId;
        public final String messageId;

        public ArendeData(String certificateId, String message) {
            this.intygsId = certificateId;
            this.arende = message;
            this.personId = PERSON_ID;
            this.enhetsId = UNIT_ID;
            this.messageId = UUID.randomUUID().toString();
        }
    }
}
