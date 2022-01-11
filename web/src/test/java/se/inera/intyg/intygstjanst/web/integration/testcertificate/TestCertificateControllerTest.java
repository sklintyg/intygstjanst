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
package se.inera.intyg.intygstjanst.web.integration.testcertificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.infra.testcertificate.dto.TestCertificateEraseRequest;
import se.inera.intyg.infra.testcertificate.dto.TestCertificateEraseResult;
import se.inera.intyg.intygstjanst.web.service.TestCertificateService;

@RunWith(MockitoJUnitRunner.class)
public class TestCertificateControllerTest {
    @Mock
    private TestCertificateService testCertificateService;

    @InjectMocks
    private TestCertificateController testCertificateController;

    @Test
    public void testEraseTestCertificateSuccessful() throws Exception {
        final var testCertificateEraseRequest = new TestCertificateEraseRequest();
        testCertificateEraseRequest.setFrom(null);
        testCertificateEraseRequest.setTo(LocalDateTime.now());

        final var testCertificateEraseResult = TestCertificateEraseResult.create(0, 0);

        doReturn(testCertificateEraseResult).when(testCertificateService).eraseTestCertificates(any(), any());

        final Response actualResponse = testCertificateController.eraseTestCertificates(testCertificateEraseRequest);

        assertNotNull(actualResponse);
        assertEquals(200, actualResponse.getStatus());
        assertTrue(actualResponse.hasEntity());
    }

    @Test
    public void testEraseTestCertificateMissingToDate() throws Exception {
        final var testCertificateEraseRequest = new TestCertificateEraseRequest();
        testCertificateEraseRequest.setFrom(null);
        testCertificateEraseRequest.setTo(null);

        final var actualResponse = testCertificateController.eraseTestCertificates(testCertificateEraseRequest);

        assertNotNull(actualResponse);
        assertEquals(400, actualResponse.getStatus());
    }

    @Test
    public void testEraseTestCertificateIncorrectDateRange() throws Exception {
        final var testCertificateEraseRequest = new TestCertificateEraseRequest();
        testCertificateEraseRequest.setFrom(LocalDateTime.now());
        testCertificateEraseRequest.setTo(testCertificateEraseRequest.getFrom().minusDays(1));

        final var actualResponse = testCertificateController.eraseTestCertificates(testCertificateEraseRequest);

        assertNotNull(actualResponse);
        assertEquals(400, actualResponse.getStatus());
    }
}
