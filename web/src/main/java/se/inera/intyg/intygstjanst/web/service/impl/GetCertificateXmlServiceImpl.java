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

package se.inera.intyg.intygstjanst.web.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.intygstjanst.web.service.GetCertificateXmlService;
import se.inera.intyg.intygstjanst.web.service.dto.GetCertificateXmlResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetCertificateXmlServiceImpl implements GetCertificateXmlService {

    @Value("${certificateservice.base.url}")
    private String csBaseUrl;

    private static final String ENDPOINT_URL = "/internalapi/certificate/{certificateId}/xml";

    private final RestTemplate restTemplate;

    @Override
    public GetCertificateXmlResponse get(String certificateId) {
        try {
            final var url = csBaseUrl + ENDPOINT_URL;
            return restTemplate.postForObject(url, HttpEntity.EMPTY, GetCertificateXmlResponse.class, certificateId);
        } catch (RestClientException e) {
            log.error("Failure fetching xml from CertificateService for certificate id '{}'.", certificateId, e);
            throw e;
        }
    }
}
