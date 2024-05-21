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

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.intygstjanst.web.csintegration.dto.CertificateExistsResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateMetadataResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesRequest;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetMessageXmlResponse;

@Service
@RequiredArgsConstructor
public class CSIntegrationService {

    private static final String CITIZEN_ENDPOINT_URL = "/api/citizen/certificate";
    private static final String INTERNALAPI_CERTIFICATE_XML_ENDPOINT_URL = "/internalapi/certificate/{certificateId}/xml";
    private static final String INTERNAL_MESSAGE_XML_ENDPOINT_URL = "/internalapi/message/{messageId}/xml";
    private static final String INTERNAL_CERTIFICATE_METADATA_ENDPOINT_URL = "/internalapi/certificate/{certificateId}/exists";
    private static final String INTERNAL_CERTIFICATE_EXISTS_ENDPOINT_URL = "/internalapi/certificate/{certificateId}/metadata";

    private final RestTemplate restTemplate;

    @Value("${certificateservice.base.url}")
    private String baseUrl;

    public List<Certificate> getCitizenCertificates(GetCitizenCertificatesRequest request) {
        final var url = baseUrl + CITIZEN_ENDPOINT_URL;

        final var response = restTemplate.postForObject(url, request, GetCitizenCertificatesResponse.class);

        if (response == null) {
            throw new IllegalStateException("Failed to get citizen certificates from certificate service");
        }

        return response.getCitizenCertificates();
    }

    public GetCertificateXmlResponse getCertificateXmlResponse(String certificateId) {
        final var url = baseUrl + INTERNALAPI_CERTIFICATE_XML_ENDPOINT_URL;
        return restTemplate.postForObject(url, HttpEntity.EMPTY, GetCertificateXmlResponse.class, certificateId);
    }

    public GetMessageXmlResponse getMessageXmlResponse(String messageId) {
        final var url = baseUrl + INTERNAL_MESSAGE_XML_ENDPOINT_URL;
        return restTemplate.postForObject(url, HttpEntity.EMPTY, GetMessageXmlResponse.class, messageId);
    }

    public boolean certificateExists(String certificateId) {
        try {
            final var url = baseUrl + INTERNAL_CERTIFICATE_EXISTS_ENDPOINT_URL;
            final var response = restTemplate.getForObject(url, CertificateExistsResponse.class, certificateId);
            return Objects.requireNonNull(response).isExists();
        } catch (Exception e) {
            throw new IllegalStateException("Failure calling certficateExists of certficate-service.");
        }
    }

    public CertificateMetadata getCertificateMetadata(String certificateId) {
        try {
            final var url = baseUrl + INTERNAL_CERTIFICATE_METADATA_ENDPOINT_URL;
            final var response = restTemplate.getForObject(url, GetCertificateMetadataResponse.class, certificateId);
            return Objects.requireNonNull(response).getCertificateMetadata();
        } catch (Exception e) {
            throw new IllegalStateException("Failure getting certificate metadata from certficate-service.");
        }
    }
}
