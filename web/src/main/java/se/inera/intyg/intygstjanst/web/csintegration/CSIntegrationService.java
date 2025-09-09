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

package se.inera.intyg.intygstjanst.web.csintegration;

import static se.inera.intyg.intygstjanst.logging.MdcHelper.LOG_SESSION_ID_HEADER;
import static se.inera.intyg.intygstjanst.logging.MdcHelper.LOG_TRACE_ID_HEADER;
import static se.inera.intyg.intygstjanst.logging.MdcLogConstants.SESSION_ID_KEY;
import static se.inera.intyg.intygstjanst.logging.MdcLogConstants.TRACE_ID_KEY;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.intygstjanst.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.web.csintegration.dto.CertificateExistsResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.ExportCertificateInternalResponseDTO;
import se.inera.intyg.intygstjanst.web.csintegration.dto.ExportCertificatesRequestDTO;
import se.inera.intyg.intygstjanst.web.csintegration.dto.ExportInternalResponseDTO;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateMetadataResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesRequest;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetMessageXmlResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SendCitizenCertificateRequestDTO;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SendCitizenCertificateResponseDTO;
import se.inera.intyg.intygstjanst.web.csintegration.dto.TotalExportsInternalResponseDTO;

@Service
@RequiredArgsConstructor
public class CSIntegrationService {

    private static final String CITIZEN_ENDPOINT_URL = "/api/citizen/certificate";
    private static final String INTERNALAPI_GET_CERTIFICATE_URL = "/internalapi/certificate/{certificateId}";
    private static final String CITIZEN_ENDPOINT_URL_SEND = "/api/citizen/certificate/{certificateId}/send";
    private static final String INTERNALAPI_CERTIFICATE_XML_ENDPOINT_URL = "/internalapi/certificate/{certificateId}/xml";
    private static final String INTERNAL_MESSAGE_XML_ENDPOINT_URL = "/internalapi/message/{messageId}/xml";
    private static final String INTERNAL_CERTIFICATE_METADATA_ENDPOINT_URL = "/internalapi/certificate/{certificateId}/metadata";
    private static final String INTERNAL_CERTIFICATE_EXISTS_ENDPOINT_URL = "/internalapi/certificate/{certificateId}/exists";
    private static final String INTERNALAPI_EXPORT_CERTIFICATE_CAREPROVIDER_ENDPOINT_URL = "/internalapi/certificate/export/{careProviderId}";
    private static final String INTERNALAPI_TOTAL_EXPORT_CERTIFICATE_CAREPROVIDER_ENDPOINT_URL = "/internalapi/certificate/export/{careProviderId}/total";
    private static final String INTERNALAPI_ERASE_CERTIFICATE_CAREPROVIDER_ENDPOINT_URL = "/internalapi/certificate/erase/{careProviderId}";

    private final RestClient csRestClient;

    @PerformanceLogging(eventAction = "list-certificates-for-citizen", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED, isActive = false)
    public List<Certificate> getCitizenCertificates(GetCitizenCertificatesRequest request) {
        final var response = csRestClient
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

    @PerformanceLogging(eventAction = "retrieve-certificate", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED, isActive = false)
    public Certificate getCertificate(String id) {
        final var response = csRestClient
            .post()
            .uri(INTERNALAPI_GET_CERTIFICATE_URL, id)
            .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(GetCertificateResponse.class);

        if (response == null) {
            throw new IllegalStateException("Failed to get certificate from certificate service");
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "retrieve-certificate-xml", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED, isActive = false)
    public GetCertificateXmlResponse getCertificateXmlResponse(String certificateId) {
        return csRestClient
            .post()
            .uri(INTERNALAPI_CERTIFICATE_XML_ENDPOINT_URL, certificateId)
            .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(GetCertificateXmlResponse.class);
    }

    @PerformanceLogging(eventAction = "retrieve-message-xml", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED, isActive = false)
    public GetMessageXmlResponse getMessageXmlResponse(String messageId) {
        return csRestClient
            .post()
            .uri(INTERNAL_MESSAGE_XML_ENDPOINT_URL, messageId)
            .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(GetMessageXmlResponse.class);
    }

    @PerformanceLogging(eventAction = "retrieve-certificate-exists", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED, isActive = false)
    public boolean certificateExists(String certificateId) {
        final var response = csRestClient
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

    @PerformanceLogging(eventAction = "retrieve-certificate-metadata", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED, isActive = false)
    public CertificateMetadata getCertificateMetadata(String certificateId) {
        final var response = csRestClient
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

    @PerformanceLogging(eventAction = "send-certificate-for-citizen", eventType = MdcLogConstants.EVENT_TYPE_CHANGE, isActive = false)
    public Certificate sendCitizenCertificates(SendCitizenCertificateRequestDTO request, String certificateId) {
        final var response = csRestClient
            .post()
            .uri(CITIZEN_ENDPOINT_URL_SEND, certificateId)
            .body(request)
            .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(SendCitizenCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("Failed to send citizen certificates from certificate service");
        }

        return response.getCitizenCertificate();
    }

    public List<ExportCertificateInternalResponseDTO> getInternalExportCertificatesForCareProvider(ExportCertificatesRequestDTO request,
        String careProviderId) {
        final var response = csRestClient
            .post()
            .uri(INTERNALAPI_EXPORT_CERTIFICATE_CAREPROVIDER_ENDPOINT_URL, careProviderId)
            .body(request)
            .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(ExportInternalResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("Failed to retrieve exports from certificate service");
        }

        return response.getExports();
    }

    public TotalExportsInternalResponseDTO getInternalTotalExportForCareProvider(
        String careProviderId) {
        final var response = csRestClient
            .get()
            .uri(INTERNALAPI_TOTAL_EXPORT_CERTIFICATE_CAREPROVIDER_ENDPOINT_URL, careProviderId)
            .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .retrieve()
            .body(TotalExportsInternalResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("Failed to retrieve total exports from certificate service");
        }

        return response;
    }

    public void eraseCertificatesForCareProvider(String careProviderId) {
        final var response = csRestClient
            .delete()
            .uri(INTERNALAPI_ERASE_CERTIFICATE_CAREPROVIDER_ENDPOINT_URL, careProviderId)
            .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .retrieve()
            .toBodilessEntity();

        if (response.getStatusCode().isError()) {
            throw new IllegalStateException("Failed to erase certificates from certificate service");
        }
    }
}