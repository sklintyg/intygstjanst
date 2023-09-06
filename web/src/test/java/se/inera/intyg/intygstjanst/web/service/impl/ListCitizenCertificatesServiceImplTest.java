package se.inera.intyg.intygstjanst.web.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.repo.CitizenCertificatesRepositoryImpl;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCitizenCertificatesServiceImplTest {

    private final static List<CitizenCertificate> response = List.of(CitizenCertificate.builder().build());
    private final static String PATIENT_ID = "191212121212";
    private final static List<String> UNITS = List.of("Unit 1", "Unit 2");
    private final static List<String> CERTIFICATE_TYPES = List.of("lisjp", "ag7804");
    private final static List<String> YEARS = List.of("2020", "2021");
    private final static List<CitizenCertificateStatusTypeDTO> STATUSES = List.of(CitizenCertificateStatusTypeDTO.SENT, CitizenCertificateStatusTypeDTO.NOT_SENT);

    @Mock
    CitizenCertificatesRepositoryImpl citizenCertificatesRepository;

    @InjectMocks
    ListCitizenCertificatesServiceImpl listCitizenCertificatesService;

    @Nested
    class RepositoryRequest {
        @Test
        void shouldSetPatientId() {
            listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

            final var captor = ArgumentCaptor.forClass(String.class);

            verify(citizenCertificatesRepository)
                    .getCertificatesForPatient(captor.capture(), anyList(), anyList(), anyList(), anyList());

            assertEquals(PATIENT_ID, captor.getValue());
        }

        @Test
        void shouldSetCertificateTypes() {
            listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

            final var captor = ArgumentCaptor.forClass(List.class);

            verify(citizenCertificatesRepository)
                    .getCertificatesForPatient(anyString(), captor.capture(), anyList(), anyList(), anyList());

            assertEquals(CERTIFICATE_TYPES, captor.getValue());
        }

        @Test
        void shouldSetUnits() {
            listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

            final var captor = ArgumentCaptor.forClass(List.class);

            verify(citizenCertificatesRepository)
                    .getCertificatesForPatient(anyString(), anyList(), captor.capture(), anyList(), anyList());

            assertEquals(UNITS, captor.getValue());
        }

        @Test
        void shouldSetStatuses() {
            listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

            final var captor = ArgumentCaptor.forClass(List.class);

            verify(citizenCertificatesRepository)
                    .getCertificatesForPatient(anyString(), anyList(), anyList(), captor.capture(), anyList());

            assertEquals(STATUSES, captor.getValue());
        }

        @Test
        void shouldSetYears() {
            listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

            final var captor = ArgumentCaptor.forClass(List.class);

            verify(citizenCertificatesRepository)
                    .getCertificatesForPatient(anyString(), anyList(), anyList(), anyList(), captor.capture());

            assertEquals(YEARS, captor.getValue());
        }
    }

    @Nested
    class Response {
        @BeforeEach
        void setup() {
            when(citizenCertificatesRepository.getCertificatesForPatient(anyString(), anyList(), anyList(), anyList(), anyList()))
                    .thenReturn(response);
        }

        @Test
        void shouldReturnResponseFromRepository() {
            final var actualResponse = listCitizenCertificatesService.get(PATIENT_ID, CERTIFICATE_TYPES, UNITS, STATUSES, YEARS);

            assertEquals(response, actualResponse);
        }
    }

}