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

import static se.inera.intyg.intygstjanst.logging.MdcHelper.LOG_SESSION_ID_HEADER;
import static se.inera.intyg.intygstjanst.logging.MdcHelper.LOG_TRACE_ID_HEADER;
import static se.inera.intyg.intygstjanst.logging.MdcLogConstants.*;

import java.util.List;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.intygstjanst.web.csintegration.dto.CertificateExistsResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateMetadataResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesRequest;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetMessageXmlResponse;

@Service
public class CSIntegrationService {

    private static final String CITIZEN_ENDPOINT_URL = "/api/citizen/certificate";
    private static final String INTERNALAPI_CERTIFICATE_XML_ENDPOINT_URL = "/internalapi/certificate/{certificateId}/xml";
    private static final String INTERNAL_MESSAGE_XML_ENDPOINT_URL = "/internalapi/message/{messageId}/xml";
    private static final String INTERNAL_CERTIFICATE_METADATA_ENDPOINT_URL = "/internalapi/certificate/{certificateId}/metadata";
    private static final String INTERNAL_CERTIFICATE_EXISTS_ENDPOINT_URL = "/internalapi/certificate/{certificateId}/exists";

    @Value("${certificateservice.base.url}")
    private String baseUrl;

    public List<Certificate> getCitizenCertificates(GetCitizenCertificatesRequest request) {
        final var response = RestClient.create(baseUrl)
            .post()
            .uri(CITIZEN_ENDPOINT_URL)
            .body(request)
            .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(GetCitizenCertificatesResponse.class);

        if (response == null) {
            throw new IllegalStateException("Failed to get citizen certificates from certificate service");
        }

        return response.getCitizenCertificates();
    }

    public GetCertificateXmlResponse getCertificateXmlResponse(String certificateId) {
        return RestClient.create(baseUrl)
            .post()
            .uri(INTERNALAPI_CERTIFICATE_XML_ENDPOINT_URL, certificateId)
            .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(GetCertificateXmlResponse.class);
    }

    public GetMessageXmlResponse getMessageXmlResponse(String messageId) {
        return RestClient.create(baseUrl)
            .post()
            .uri(INTERNAL_MESSAGE_XML_ENDPOINT_URL, messageId)
            .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(GetMessageXmlResponse.class);
    }

    public boolean certificateExists(String certificateId) {
        final var response = RestClient.create(baseUrl)
            .get()
            .uri(INTERNAL_CERTIFICATE_EXISTS_ENDPOINT_URL, certificateId)
            .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .retrieve()
            .body(CertificateExistsResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                String.format("Failure calling certficateExists of certficate-service for certificateId '%s'.", certificateId)
            );
        }

        return response.isExists();
    }

    public CertificateMetadata getCertificateMetadata(String certificateId) {
        final var response = RestClient.create(baseUrl)
            .get()
            .uri(INTERNAL_CERTIFICATE_METADATA_ENDPOINT_URL, certificateId)
            .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .retrieve()
            .body(GetCertificateMetadataResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                String.format("Failure getting certificate metadata from certficate-service for certificateId '%s'.", certificateId)
            );
        }

        return response.getCertificateMetadata();
    }
}
