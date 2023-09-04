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

package se.inera.intyg.intygstjanst.web.integration.citizen;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.service.ListCitizenCertificatesService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateControllerTest {

    @Mock
    private ListCitizenCertificatesService listCitizenCertificatesService;
    @InjectMocks
    private CitizenCertificateController citizenCertificateController;

    @Nested
    class ListCitizenCertificates {

        private ListCitizenCertificatesRequestDTO request = ListCitizenCertificatesRequestDTO
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
            void shouldSendPatientId() {
                citizenCertificateController.getCitizenCertificates(request);

                final var captor = ArgumentCaptor.forClass(String.class);

                verify(listCitizenCertificatesService).get(captor.capture(), anyList(), anyList(), anyList(), anyList());

                assertEquals(request.getPatientId(), captor.getValue());
            }
        }

        @Nested
        class Response {

        }
    }
}
