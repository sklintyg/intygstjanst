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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateUnitDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.ListCitizenCertificatesRequest;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateFilterServiceImplTest {

    private static final CitizenCertificateDTO CITIZEN_CERTIFICATE = CitizenCertificateDTO
        .builder()
        .issued(LocalDateTime.of(2020, 1, 20, 20, 20, 20))
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
            final var response = citizenCertificateFilterService.filter(
                CITIZEN_CERTIFICATE,
                ListCitizenCertificatesRequest
                    .builder()
                    .years(List.of("2020", "2021"))
                    .build()
            );

            assertTrue(response);
        }

        @Test
        void shouldReturnFalseIfIncludedInList() {
            final var response = citizenCertificateFilterService.filter(
                CITIZEN_CERTIFICATE,
                ListCitizenCertificatesRequest
                    .builder()
                    .years(List.of("2019", "2021"))
                    .build()
            );
            assertFalse(response);
        }

        @Test
        void shouldReturnTrueIfEmptyList() {
            final var response = citizenCertificateFilterService.filter(
                CITIZEN_CERTIFICATE,
                ListCitizenCertificatesRequest
                    .builder()
                    .build()
            );
            assertTrue(response);
        }
    }

    @Nested
    class FilterUnitId {

        @Test
        void shouldReturnTrueIfIncludedInList() {

            final var response = citizenCertificateFilterService.filter(
                CITIZEN_CERTIFICATE,
                ListCitizenCertificatesRequest
                    .builder()
                    .units(List.of("UNIT_ID", "NON_UNIT_ID"))
                    .build()
            );

            assertTrue(response);
        }

        @Test
        void shouldReturnFalseIfIncludedInList() {
            final var response = citizenCertificateFilterService.filter(
                CITIZEN_CERTIFICATE,
                ListCitizenCertificatesRequest
                    .builder()
                    .units(List.of("NON_UNIT_ID"))
                    .build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnTrueIfEmptyList() {
            final var response = citizenCertificateFilterService.filter(
                CITIZEN_CERTIFICATE,
                ListCitizenCertificatesRequest
                    .builder()
                    .build()
            );

            assertTrue(response);
        }
    }

    @Nested
    class FilterCertificateType {

        @Test
        void shouldReturnTrueIfIncludedInList() {

            final var response = citizenCertificateFilterService.filter(
                CITIZEN_CERTIFICATE,
                ListCitizenCertificatesRequest
                    .builder()
                    .certificateTypes(List.of("TYPE_ID", "NON_TYPE_ID"))
                    .build()
            );

            assertTrue(response);
        }

        @Test
        void shouldReturnFalseIfIncludedInList() {
            final var response = citizenCertificateFilterService.filter(
                CITIZEN_CERTIFICATE,
                ListCitizenCertificatesRequest
                    .builder()
                    .units(List.of("NON_TYPE_ID"))
                    .build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnTrueIfEmptyList() {
            final var response = citizenCertificateFilterService.filter(
                CITIZEN_CERTIFICATE,
                ListCitizenCertificatesRequest
                    .builder()
                    .build()
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
                final var response = citizenCertificateFilterService.filter(
                    certificate,
                    ListCitizenCertificatesRequest
                        .builder()
                        .build()
                );

                assertTrue(response);
            }

            @Test
            void shouldReturnFalseIfFilteringOnSent() {
                final var response = citizenCertificateFilterService.filter(
                    certificate,
                    ListCitizenCertificatesRequest
                        .builder()
                        .statuses(List.of(CitizenCertificateStatusTypeDTO.SENT))
                        .build()
                );

                assertFalse(response);
            }

            @Test
            void shouldReturnFalseIfFilteringOnBothStatuses() {
                final var response = citizenCertificateFilterService.filter(
                    certificate,
                    ListCitizenCertificatesRequest
                        .builder()
                        .statuses(List.of(CitizenCertificateStatusTypeDTO.SENT, CitizenCertificateStatusTypeDTO.NOT_SENT))
                        .build()
                );

                assertFalse(response);
            }

            @Test
            void shouldReturnFalseIfFilteringOnNotSent() {
                final var response = citizenCertificateFilterService.filter(
                    certificate,
                    ListCitizenCertificatesRequest
                        .builder()
                        .statuses(List.of(CitizenCertificateStatusTypeDTO.NOT_SENT))
                        .build()
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
                        .sent(LocalDateTime.now())
                        .build()
                )
                .build();

            @Test
            void shouldReturnTrueIfListIsEmpty() {
                final var response = citizenCertificateFilterService.filter(
                    certificate,
                    ListCitizenCertificatesRequest
                        .builder()
                        .build()
                );

                assertTrue(response);
            }

            @Test
            void shouldReturnTrueIfFilteringOnSent() {
                final var response = citizenCertificateFilterService.filter(
                    certificate,
                    ListCitizenCertificatesRequest
                        .builder()
                        .statuses(List.of(CitizenCertificateStatusTypeDTO.SENT))
                        .build()
                );

                assertTrue(response);
            }

            @Test
            void shouldReturnFalseIfFilteringOnNotSent() {
                final var response = citizenCertificateFilterService.filter(
                    certificate,
                    ListCitizenCertificatesRequest
                        .builder()
                        .statuses(List.of(CitizenCertificateStatusTypeDTO.NOT_SENT))
                        .build()
                );

                assertFalse(response);
            }

            @Test
            void shouldReturnTrueIfFilteringOnAllStatuses() {
                final var response = citizenCertificateFilterService.filter(
                    certificate,
                    ListCitizenCertificatesRequest
                        .builder()
                        .statuses(List.of(CitizenCertificateStatusTypeDTO.SENT, CitizenCertificateStatusTypeDTO.NOT_SENT))
                        .build()
                );

                assertTrue(response);
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
                final var response = citizenCertificateFilterService.filter(
                    certificate,
                    ListCitizenCertificatesRequest
                        .builder()
                        .statuses(Collections.emptyList())
                        .build()
                );

                assertTrue(response);
            }

            @Test
            void shouldReturnFalseIfFilteringOnSent() {
                final var response = citizenCertificateFilterService.filter(
                    certificate,
                    ListCitizenCertificatesRequest
                        .builder()
                        .statuses(List.of(CitizenCertificateStatusTypeDTO.SENT))
                        .build()
                );

                assertFalse(response);
            }

            @Test
            void shouldReturnTrueIfFilteringOnNotSent() {
                final var response = citizenCertificateFilterService.filter(
                    certificate,
                    ListCitizenCertificatesRequest
                        .builder()
                        .statuses(List.of(CitizenCertificateStatusTypeDTO.NOT_SENT))
                        .build()
                );

                assertTrue(response);
            }

            @Test
            void shouldReturnTrueIfFilteringOnAllStatuses() {
                final var response = citizenCertificateFilterService.filter(
                    certificate,
                    ListCitizenCertificatesRequest
                        .builder()
                        .statuses(List.of(CitizenCertificateStatusTypeDTO.SENT, CitizenCertificateStatusTypeDTO.NOT_SENT))
                        .build()
                );

                assertTrue(response);
            }
        }
    }

}