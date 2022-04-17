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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil.IntegrationTestCertificateType.LISJP;

import io.restassured.mapper.TypeRef;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.http.HttpStatus;
import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateExportPageDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateTextDTO;

public class CertificateExportIT extends BaseIntegrationTest {

    private static final String INTERNAL_BASE_URI = System.getProperty("integration.tests.actuatorUrl", "http://localhost:8180");
    private static final String CERTIFICATE_TEXTS_URL = INTERNAL_BASE_URI + "/inera-certificate/internalapi/v1/certificatetexts";
    private static final String CERTIFICATES_URL = INTERNAL_BASE_URI + "/inera-certificate/internalapi/v1/certificates/";
    private static final String PERSON_ID = "191212121212";
    private static final String LISJP_VERSION = "1.3";
    private static final String CARE_PROVIDER_ID = "SE2321000016-39KJ";

    private static final int PAGE1 = 0;
    private static final int PAGE2 = 1;
    private static final int PAGE_SIZE = 3;
    private static final int CERTIFICATES_TOTAL = 4;

    private static final TypeRef<List<CertificateTextDTO>> CERTIFICATE_TEXTS = new TypeRef<>() { };

    @Nested
    class CertificateTexts {

        @Test
        public void shouldSuccessfullyFetchCertificateTexts() {
            final var texts = given()
                .when().get(CERTIFICATE_TEXTS_URL)
                .then().statusCode(HttpStatus.OK.value())
                .extract().response().getBody().as(CERTIFICATE_TEXTS);

            assertAll(
                () -> assertTrue(texts.size() >= 29)
            );
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class Certificates {

        @BeforeAll
        public void setup() {
            setupCertificates();
            setupRevoked();
        }

        @AfterAll
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
                () -> assertEquals(CERTIFICATES_TOTAL, page.getTotal()),
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
                () -> assertEquals(CERTIFICATES_TOTAL, page.getTotal()),
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

    private void setupCertificates() {
        for (int i = 0; i < CERTIFICATES_TOTAL; i++) {
            IntegrationTestUtil.registerCertificateFromTemplate("CertificateExportIT-" + i, LISJP_VERSION, PERSON_ID, LISJP);
        }
    }

    private void setupRevoked() {
        IntegrationTestUtil.revokeCertificate("CertificateExportIT-2", PERSON_ID);
    }

    private void deleteCertificates() {
        for (int i = 0; i < CERTIFICATES_TOTAL; i++) {
            IntegrationTestUtil.deleteIntyg("CertificateExportIT-" + i);
        }
    }
}
