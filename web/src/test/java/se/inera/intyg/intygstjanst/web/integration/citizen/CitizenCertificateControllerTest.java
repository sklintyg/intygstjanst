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

package se.inera.intyg.intygstjanst.web.integration.citizen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.service.ListCitizenCertificatesService;
import se.inera.intyg.intygstjanst.web.service.SendCertificateService;
import se.inera.intyg.intygstjanst.web.service.dto.SendCertificateRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.ListCitizenCertificatesRequest;
import se.inera.intyg.schemas.contract.Personnummer;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateControllerTest {

    @Mock
    private ListCitizenCertificatesService listCitizenCertificatesService;
    @Mock
    private SendCertificateService sendCertificateService;
    @InjectMocks
    private CitizenCertificateController citizenCertificateController;

    @Nested
    class ListCitizenCertificates {

        private final CitizenCertificatesRequestDTO request = CitizenCertificatesRequestDTO
            .builder()
            .patientId("191212121212")
            .certificateTypes(List.of("Lisjp", "Ag7804"))
            .statuses(List.of(CitizenCertificateStatusTypeDTO.NOT_SENT, CitizenCertificateStatusTypeDTO.SENT))
            .units(List.of("UNIT_1"))
            .years(List.of("2020"))
            .build();

        @Nested
        class Request {

            @Test
            void shouldSetPatientId() {
                citizenCertificateController.getCitizenCertificates(request);

                final var captor = ArgumentCaptor.forClass(
                    ListCitizenCertificatesRequest.class
                );

                verify(listCitizenCertificatesService).get(captor.capture());

                assertEquals(request.getPatientId(), captor.getValue().getPersonnummer().getOriginalPnr());
            }

            @Test
            void shouldSetCertificateTypes() {
                citizenCertificateController.getCitizenCertificates(request);

                final var captor = ArgumentCaptor.forClass(
                    ListCitizenCertificatesRequest.class
                );

                verify(listCitizenCertificatesService).get(captor.capture());

                assertEquals(request.getCertificateTypes(), captor.getValue().getCertificateTypes());
            }

            @Test
            void shouldSetStatuses() {
                citizenCertificateController.getCitizenCertificates(request);

                final var captor = ArgumentCaptor.forClass(
                    ListCitizenCertificatesRequest.class
                );

                verify(listCitizenCertificatesService).get(captor.capture());

                assertEquals(request.getStatuses(), captor.getValue().getStatuses());
            }

            @Test
            void shouldSetUnits() {
                citizenCertificateController.getCitizenCertificates(request);

                final var captor = ArgumentCaptor.forClass(
                    ListCitizenCertificatesRequest.class
                );

                verify(listCitizenCertificatesService).get(captor.capture());

                assertEquals(request.getUnits(), captor.getValue().getUnits());
            }

            @Test
            void shouldSetYears() {
                citizenCertificateController.getCitizenCertificates(request);

                final var captor = ArgumentCaptor.forClass(
                    ListCitizenCertificatesRequest.class
                );

                verify(listCitizenCertificatesService).get(captor.capture());

                assertEquals(request.getYears(), captor.getValue().getYears());
            }
        }

        @Nested
        class Response {

            CitizenCertificateDTO expectedCertificate = CitizenCertificateDTO.builder().build();
            List<CitizenCertificateDTO> expectedContent = List.of(expectedCertificate);

            @BeforeEach
            void setup() {
                Mockito
                    .when(listCitizenCertificatesService.get(any()))
                    .thenReturn(expectedContent);
            }

            @Test
            void shouldReturnContent() {
                final var response = citizenCertificateController.getCitizenCertificates(request);

                assertEquals(expectedContent, response.getContent());
                assertEquals(expectedContent.size(), response.getContent().size());
                assertEquals(expectedContent.get(0), response.getContent().get(0));
            }
        }
    }

    @Nested
    class SendCertificate {

        private final CitizenCertificateSendRequestDTO request = CitizenCertificateSendRequestDTO
            .builder()
            .certificateId("certificateId")
            .patientId("191212121212")
            .recipient("recipient")
            .build();

        @SneakyThrows
        @Test
        void shouldSendCertificateId() {
            citizenCertificateController.sendCitizenCertificate(request);

            final var captor = ArgumentCaptor.forClass(SendCertificateRequestDTO.class);

            verify(sendCertificateService).send(captor.capture());
            assertEquals(request.getCertificateId(), captor.getValue().getCertificateId());
        }

        @SneakyThrows
        @Test
        void shouldSendRecipient() {
            citizenCertificateController.sendCitizenCertificate(request);

            final var captor = ArgumentCaptor.forClass(SendCertificateRequestDTO.class);

            verify(sendCertificateService).send(captor.capture());
            assertEquals(request.getRecipient(), captor.getValue().getRecipientId());
        }

        @SneakyThrows
        @Test
        void shouldSendPatientId() {
            citizenCertificateController.sendCitizenCertificate(request);

            final var captor = ArgumentCaptor.forClass(SendCertificateRequestDTO.class);

            verify(sendCertificateService).send(captor.capture());
            assertEquals(
                Personnummer.createPersonnummer(request.getPatientId()).get(),
                captor.getValue().getPatientId()
            );
        }
    }
}
