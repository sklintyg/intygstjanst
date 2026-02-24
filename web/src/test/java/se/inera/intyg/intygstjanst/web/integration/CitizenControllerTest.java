/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.integration.CitizenController.RequestObject;
import se.inera.intyg.intygstjanst.web.integration.CitizenController.ResponseObject;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RelationService;

@ExtendWith(MockitoExtension.class)
class CitizenControllerTest {

    @Mock
    private CertificateService certificateService;

    @Mock
    private RelationService relationService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private CitizenController citizenController;

    @Test
    void testFilteringOfTestCertificates() {
        final RequestObject parameters = RequestObject.of("191212121212", false);

        final Certificate realCertificate = new Certificate("realCertificateId");
        final Certificate testCertificate = new Certificate("testCertificateId");
        testCertificate.setTestCertificate(true);

        List<Certificate> certificates = Arrays.asList(realCertificate, testCertificate);

        when(certificateService.listCertificatesForCitizen(any(), any(), any(), any())).thenReturn(certificates);
        final List<ResponseObject> actualResponse = citizenController.getCertificates(parameters);

        assertNotNull(actualResponse);
        assertEquals(1, actualResponse.size());
        assertNotNull(actualResponse.getFirst());
        assertEquals("realCertificateId", actualResponse.getFirst().getCertificate().getId());
    }
}
