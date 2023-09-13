/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateDTOConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateFilterService;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateTextService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.ListCitizenCertificatesRequest;
import se.inera.intyg.intygstjanst.web.service.repo.CitizenCertificatesRepositoryImpl;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;
import se.inera.intyg.schemas.contract.Personnummer;

@ExtendWith(MockitoExtension.class)
class ListCitizenCertificatesServiceImplTest {

    private static final String PATIENT_ID = "191212121212";

    private static final Personnummer PATIENT_ID_AS_PERSONNUMMER = Personnummer.createPersonnummer(PATIENT_ID).orElseThrow();
    private static final List<String> UNITS = List.of("Unit 1", "Unit 2");
    private static final List<String> CERTIFICATE_TYPES = List.of("lisjp", "ag7804");
    private static final List<String> YEARS = List.of("2020", "2021");
    private static final String TYPE_NAME = "Type name";
    private static final String ADDITIONAL_INFO_LABEL = "Additional info label";
    private static final String TYPE_ID = "Type id";
    private static final String TYPE_VERSION = "Type version";
    private static final List<CitizenCertificateStatusTypeDTO> STATUSES = List.of(
        CitizenCertificateStatusTypeDTO.SENT, CitizenCertificateStatusTypeDTO.NOT_SENT);
    private static final ListCitizenCertificatesRequest REQUEST = ListCitizenCertificatesRequest
        .builder()
        .certificateTypes(CERTIFICATE_TYPES)
        .personnummer(PATIENT_ID_AS_PERSONNUMMER)
        .years(YEARS)
        .units(UNITS)
        .statuses(STATUSES)
        .build();
    private static final List<CitizenCertificate> REPO_RESPONSE = List.of(CitizenCertificate
        .builder()
        .type(TYPE_ID)
        .typeVersion(TYPE_VERSION)
        .build()
    );
    private static final CitizenCertificateDTO CITIZEN_CERTIFICATE_DTO = CitizenCertificateDTO.builder().build();

    @Mock
    CitizenCertificatesRepositoryImpl citizenCertificatesRepository;
    @Mock
    CitizenCertificateTextService citizenCertificateTextService;
    @Mock
    CitizenCertificateDTOConverter citizenCertificateDTOConverter;
    @Mock
    CitizenCertificateFilterService citizenCertificateFilterService;
    @Mock
    MonitoringLogService monitoringLogService;

    @InjectMocks
    ListCitizenCertificatesServiceImpl listCitizenCertificatesService;

    @Nested
    class RepositoryRequest {

        @Test
        void shouldSetPatientId() {
            listCitizenCertificatesService.get(REQUEST);

            final var captor = ArgumentCaptor.forClass(String.class);

            verify(citizenCertificatesRepository)
                .getCertificatesForPatient(captor.capture());

            assertEquals(PATIENT_ID, captor.getValue());
        }
    }

    @Nested
    class Response {

        @BeforeEach
        void setup() throws ModuleNotFoundException {
            when(citizenCertificateTextService.getAdditionalInfoLabel(any(), any())).thenReturn(ADDITIONAL_INFO_LABEL);
            when(citizenCertificateTextService.getTypeName(any())).thenReturn(TYPE_NAME);

            when(citizenCertificateDTOConverter.convert(any(), any(), any())).thenReturn(CITIZEN_CERTIFICATE_DTO);

            when(citizenCertificatesRepository.getCertificatesForPatient(any()))
                .thenReturn(REPO_RESPONSE);
        }

        @Test
        void shouldLogWithPatientId() {
            listCitizenCertificatesService.get(REQUEST);

            final var captor = ArgumentCaptor.forClass(Personnummer.class);

            verify(monitoringLogService).logCertificateListedByCitizen(captor.capture());

            assertEquals(PATIENT_ID, captor.getValue().getOriginalPnr());
        }

        @Nested
        class TextService {

            @Test
            void shouldSendTypeToGetTypeName() throws ModuleNotFoundException {
                listCitizenCertificatesService.get(REQUEST);

                final var captor = ArgumentCaptor.forClass(String.class);

                verify(citizenCertificateTextService).getTypeName(captor.capture());

                assertEquals(TYPE_ID, captor.getValue());
            }

            @Test
            void shouldSendTypeToGetAdditionalInfoLabel() throws ModuleNotFoundException {
                listCitizenCertificatesService.get(REQUEST);

                final var captor = ArgumentCaptor.forClass(String.class);

                verify(citizenCertificateTextService).getAdditionalInfoLabel(captor.capture(), anyString());

                assertEquals(TYPE_ID, captor.getValue());
            }

            @Test
            void shouldSendTypeVersionToGetAdditionalInfoLabel() throws ModuleNotFoundException {
                listCitizenCertificatesService.get(REQUEST);

                final var captor = ArgumentCaptor.forClass(String.class);

                verify(citizenCertificateTextService).getAdditionalInfoLabel(anyString(), captor.capture());

                assertEquals(TYPE_VERSION, captor.getValue());
            }
        }

        @Nested
        class Converter {

            @Test
            void shouldSendTypeNameToConverter() throws ModuleNotFoundException {
                listCitizenCertificatesService.get(REQUEST);

                final var captor = ArgumentCaptor.forClass(String.class);

                verify(citizenCertificateDTOConverter).convert(any(), captor.capture(), any());

                assertEquals(TYPE_NAME, captor.getValue());
            }

            @Test
            void shouldSendAdditionalInfoLabelToConverter() throws ModuleNotFoundException {
                listCitizenCertificatesService.get(REQUEST);

                final var captor = ArgumentCaptor.forClass(String.class);

                verify(citizenCertificateDTOConverter).convert(any(), any(), captor.capture());

                assertEquals(ADDITIONAL_INFO_LABEL, captor.getValue());
            }

            @Test
            void shouldSendCitizenCertificateToConverter() throws ModuleNotFoundException {
                listCitizenCertificatesService.get(REQUEST);

                final var captor = ArgumentCaptor.forClass(CitizenCertificate.class);

                verify(citizenCertificateDTOConverter).convert(captor.capture(), any(), any());

                assertEquals(REPO_RESPONSE.get(0), captor.getValue());
            }
        }

        @Nested
        class Filter {

            @Test
            void shouldSendCertificateReturnedFromConverterToFilter() {
                listCitizenCertificatesService.get(REQUEST);

                final var captor = ArgumentCaptor.forClass(CitizenCertificateDTO.class);

                verify(citizenCertificateFilterService).filter(captor.capture(), any());

                assertEquals(CITIZEN_CERTIFICATE_DTO, captor.getValue());
            }

            @Test
            void shouldSendFilterRequestToFilter() {
                listCitizenCertificatesService.get(REQUEST);

                final var captor = ArgumentCaptor.forClass(ListCitizenCertificatesRequest.class);

                verify(citizenCertificateFilterService).filter(any(), captor.capture());

                assertEquals(REQUEST, captor.getValue());
            }
        }


        @Test
        void shouldReturnResponseIfNotFilteredOut() {
            when(citizenCertificateFilterService.filter(any(), any())).thenReturn(true);

            final var actualResponse = listCitizenCertificatesService.get(REQUEST);

            assertEquals(1, actualResponse.size());
            assertEquals(CITIZEN_CERTIFICATE_DTO, actualResponse.get(0));
        }

        @Test
        void shouldReturnEmptyListIfEverythingIsFilteredOut() {
            when(citizenCertificateFilterService.filter(any(), any())).thenReturn(false);

            final var actualResponse = listCitizenCertificatesService.get(REQUEST);

            assertEquals(0, actualResponse.size());
        }
    }

}