/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.csintegration;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesRequest;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesResponse;

@Service
@RequiredArgsConstructor
public class CSIntegrationService {

    private static final String CITIZEN_ENDPOINT_URL = "/api/citizen/certificate";

    private final RestTemplate restTemplate;

    @Value("${certificateservice.base.url}")
    private String baseUrl;

    public List<Certificate> getCitizenCertificates(GetCitizenCertificatesRequest request) {
        final var url = baseUrl + CITIZEN_ENDPOINT_URL;

        final var response = restTemplate.postForObject(url, request, GetCitizenCertificatesResponse.class);

        if (response == null) {
            return Collections.emptyList();
        }

        return response.getCitizenCertificates();
    }
}
