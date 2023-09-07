package se.inera.intyg.intygstjanst.web.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateDTOConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateFilterService;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateTextService;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.repo.CitizenCertificatesRepositoryImpl;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCitizenCertificatesServiceImplTest {

    private final static String PATIENT_ID = "191212121212";
    private final static List<String> UNITS = List.of("Unit 1", "Unit 2");
    private final static List<String> CERTIFICATE_TYPES = List.of("lisjp", "ag7804");
    private final static List<String> YEARS = List.of("2020", "2021");
    private final static String TYPE_NAME = "Type name";
    private final static String ADDITIONAL_INFO_LABEL = "Additional info label";
    private final static String TYPE_ID = "Type id";
    private final static String TYPE_VERSION = "Type version";
    private final static List<CitizenCertificate> REPO_RESPONSE = List.of(CitizenCertificate
            .builder()
            .type(TYPE_ID)
            .typeVersion(TYPE_VERSION)
            .build()
    );
    private final static CitizenCertificateDTO CITIZEN_CERTIFICATE_DTO = CitizenCertificateDTO.builder().build();
    private final static List<CitizenCertificateStatusTypeDTO> STATUSES = List.of(CitizenCertificateStatusTypeDTO.SENT, CitizenCertificateStatusTypeDTO.NOT_SENT);

    @Mock
    CitizenCertificatesRepositoryImpl citizenCertificatesRepository;

    @Mock
    CitizenCertificateTextService citizenCertificateTextService;

    @Mock
    CitizenCertificateDTOConverter citizenCertificateDTOConverter;

    @Mock
    CitizenCertificateFilterService citizenCertificateFilterService;

    @InjectMocks
    ListCitizenCertificatesServiceImpl listCitizenCertificatesService;

    @Nested
    class RepositoryRequest {
        @Test
        void shouldSetPatientId() {
            listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

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

            when(citizenCertificateDTOConverter.get(any(), any(), any())).thenReturn(CITIZEN_CERTIFICATE_DTO);

            when(citizenCertificatesRepository.getCertificatesForPatient(any()))
                    .thenReturn(REPO_RESPONSE);
        }

        @Nested
        class TextService {
            @Test
            void shouldSendTypeToGetTypeName() throws ModuleNotFoundException {
                listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

                final var captor = ArgumentCaptor.forClass(String.class);

                verify(citizenCertificateTextService).getTypeName(captor.capture());

                assertEquals(TYPE_ID, captor.getValue());
            }

            @Test
            void shouldSendTypeToGetAdditionalInfoLabel() throws ModuleNotFoundException {
                listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

                final var captor = ArgumentCaptor.forClass(String.class);

                verify(citizenCertificateTextService).getAdditionalInfoLabel(captor.capture(), anyString());

                assertEquals(TYPE_ID, captor.getValue());
            }

            @Test
            void shouldSendTypeVersionToGetAdditionalInfoLabel() throws ModuleNotFoundException {
                listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

                final var captor = ArgumentCaptor.forClass(String.class);

                verify(citizenCertificateTextService).getAdditionalInfoLabel(anyString(), captor.capture());

                assertEquals(TYPE_VERSION, captor.getValue());
            }
        }

        @Nested
        class Converter {

            @Test
            void shouldSendTypeNameToConverter() throws ModuleNotFoundException {
                listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

                final var captor = ArgumentCaptor.forClass(String.class);

                verify(citizenCertificateDTOConverter).get(any(), captor.capture(), any());

                assertEquals(TYPE_NAME, captor.getValue());
            }

            @Test
            void shouldSendAdditionalInfoLabelToConverter() throws ModuleNotFoundException {
                listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

                final var captor = ArgumentCaptor.forClass(String.class);

                verify(citizenCertificateDTOConverter).get(any(), any(), captor.capture());

                assertEquals(ADDITIONAL_INFO_LABEL, captor.getValue());
            }

            @Test
            void shouldSendCitizenCertificateToConverter() throws ModuleNotFoundException {
                listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

                final var captor = ArgumentCaptor.forClass(CitizenCertificate.class);

                verify(citizenCertificateDTOConverter).get(captor.capture(), any(), any());

                assertEquals(REPO_RESPONSE.get(0), captor.getValue());
            }
        }

        @Nested
        class Filter {
            @Test
            void shouldSendCertificateReturnedFromConverterToFilter() {
                listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

                final var captor = ArgumentCaptor.forClass(CitizenCertificateDTO.class);

                verify(citizenCertificateFilterService).filter(captor.capture(), anyList(), anyList(), anyList(), anyList());

                assertEquals(CITIZEN_CERTIFICATE_DTO, captor.getValue());
            }

            @Test
            void shouldSendYearsToFilter() {
                listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

                final var captor = ArgumentCaptor.forClass(List.class);

                verify(citizenCertificateFilterService).filter(any(), captor.capture(), anyList(), anyList(), anyList());

                assertEquals(YEARS, captor.getValue());
            }

            @Test
            void shouldSendUnitIdsToFilter() {
                listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

                final var captor = ArgumentCaptor.forClass(List.class);

                verify(citizenCertificateFilterService).filter(any(), anyList(), captor.capture(), anyList(), anyList());

                assertEquals(UNITS, captor.getValue());
            }

            @Test
            void shouldSendCertificateTypesToFilter() {
                listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

                final var captor = ArgumentCaptor.forClass(List.class);

                verify(citizenCertificateFilterService).filter(any(), anyList(), anyList(), captor.capture(), anyList());

                assertEquals(CERTIFICATE_TYPES, captor.getValue());
            }

            @Test
            void shouldSendStatusesToFilter() {
                listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

                final var captor = ArgumentCaptor.forClass(List.class);

                verify(citizenCertificateFilterService).filter(any(), anyList(), anyList(), anyList(), captor.capture());

                assertEquals(STATUSES, captor.getValue());
            }
        }


        @Test
        void shouldReturnResponseIfNotFilteredOut() {
            when(citizenCertificateFilterService.filter(any(), any(), any(), any(), any())).thenReturn(true);

            final var actualResponse = listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

            assertEquals(1, actualResponse.size());
            assertEquals(CITIZEN_CERTIFICATE_DTO, actualResponse.get(0));
        }

        @Test
        void shouldReturnEmptyListIfEverythingIsFilteredOut() {
            when(citizenCertificateFilterService.filter(any(), any(), any(), any(), any())).thenReturn(false);

            final var actualResponse = listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

            assertEquals(0, actualResponse.size());
        }
    }

}