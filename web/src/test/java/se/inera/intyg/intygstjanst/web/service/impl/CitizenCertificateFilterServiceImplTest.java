package se.inera.intyg.intygstjanst.web.service.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateUnitDTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateFilterServiceImplTest {

    private static final CitizenCertificateDTO CITIZEN_CERTIFICATE = CitizenCertificateDTO
            .builder()
            .issued(LocalDateTime.of(2020, 1, 20, 20, 20, 20).toString())
            .type(
                    CitizenCertificateTypeDTO
                            .builder()
                            .id("TYPE_ID")
                            .build()
            )
            .unit(CitizenCertificateUnitDTO
                    .builder()
                    .id("UNIT_ID")
                    .build())
            .build();

    @InjectMocks
    CitizenCertificateFilterServiceImpl citizenCertificateFilterService;

    @Nested
    class FilterYears {
        @Test
        void shouldReturnTrueIfIncludedInList() {
            final var response = citizenCertificateFilterService.filterOnYears(CITIZEN_CERTIFICATE, List.of("2020", "2021"));

            assertTrue(response);
        }

        @Test
        void shouldReturnFalseIfIncludedInList() {
            final var response = citizenCertificateFilterService.filterOnYears(CITIZEN_CERTIFICATE, List.of("2019", "2021"));

            assertFalse(response);
        }

        @Test
        void shouldReturnTrueIfEmptyList() {
            final var response = citizenCertificateFilterService.filterOnYears(CITIZEN_CERTIFICATE, Collections.emptyList());

            assertTrue(response);
        }
    }

    @Nested
    class FilterUnitId {
        @Test
        void shouldReturnTrueIfIncludedInList() {
            final var response = citizenCertificateFilterService.filterOnUnits(
                    CITIZEN_CERTIFICATE, List.of("UNIT_ID", "NON_UNIT_ID")
            );

            assertTrue(response);
        }

        @Test
        void shouldReturnFalseIfIncludedInList() {
            final var response = citizenCertificateFilterService.filterOnUnits(
                    CITIZEN_CERTIFICATE, List.of("NON_UNIT_ID")
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnTrueIfEmptyList() {
            final var response = citizenCertificateFilterService.filterOnUnits(
                    CITIZEN_CERTIFICATE, Collections.emptyList()
            );

            assertTrue(response);
        }
    }

    @Nested
    class FilterStatuses {

        @Nested
        class RecipientIsNull {
            final CitizenCertificateDTO certificate = CitizenCertificateDTO
                    .builder()
                    .recipient(null)
                    .build();

            @Test
            void shouldReturnTrueIfListIsEmpty() {
                final var response = citizenCertificateFilterService.filterOnCertificateTypes(
                        certificate, Collections.emptyList()
                );

                assertTrue(response);
            }

            @Test
            void shouldReturnFalseIfFilteringOnSent() {
                final var response = citizenCertificateFilterService.filterOnSentStatus(
                        certificate, List.of(CitizenCertificateStatusTypeDTO.SENT)
                );

                assertFalse(response);
            }

            @Test
            void shouldReturnFalseIfFilteringOnNotSent() {
                final var response = citizenCertificateFilterService.filterOnSentStatus(
                        certificate, List.of(CitizenCertificateStatusTypeDTO.NOT_SENT)
                );

                assertFalse(response);
            }
        }

        @Nested
        class RecipientExistsAndSent {
            final CitizenCertificateDTO certificate = CitizenCertificateDTO
                    .builder()
                    .recipient(
                            CitizenCertificateRecipientDTO
                                    .builder()
                                    .name("Name")
                                    .id("Id")
                                    .sent(LocalDateTime.now().toString())
                                    .build()
                    )
                    .build();

            @Test
            void shouldReturnTrueIfListIsEmpty() {
                final var response = citizenCertificateFilterService.filterOnCertificateTypes(
                        certificate, Collections.emptyList()
                );

                assertTrue(response);
            }

            @Test
            void shouldReturnTrueIfFilteringOnSent() {
                final var response = citizenCertificateFilterService.filterOnSentStatus(
                        certificate, List.of(CitizenCertificateStatusTypeDTO.SENT)
                );

                assertTrue(response);
            }

            @Test
            void shouldReturnFalseIfFilteringOnNotSent() {
                final var response = citizenCertificateFilterService.filterOnSentStatus(
                        certificate, List.of(CitizenCertificateStatusTypeDTO.NOT_SENT)
                );

                assertFalse(response);
            }
        }

        @Nested
        class RecipientExistsAndNotSent {
            final CitizenCertificateDTO certificate = CitizenCertificateDTO
                    .builder()
                    .recipient(
                            CitizenCertificateRecipientDTO
                                    .builder()
                                    .name("Name")
                                    .id("Id")
                                    .sent(null)
                                    .build()
                    )
                    .build();

            @Test
            void shouldReturnTrueIfListIsEmpty() {
                final var response = citizenCertificateFilterService.filterOnCertificateTypes(
                        certificate, Collections.emptyList()
                );

                assertTrue(response);
            }

            @Test
            void shouldReturnFalseIfFilteringOnSent() {
                final var response = citizenCertificateFilterService.filterOnSentStatus(
                        certificate, List.of(CitizenCertificateStatusTypeDTO.SENT)
                );

                assertFalse(response);
            }

            @Test
            void shouldReturnTrueIfFilteringOnNotSent() {
                final var response = citizenCertificateFilterService.filterOnSentStatus(
                        certificate, List.of(CitizenCertificateStatusTypeDTO.NOT_SENT)
                );

                assertTrue(response);
            }
        }
    }

}