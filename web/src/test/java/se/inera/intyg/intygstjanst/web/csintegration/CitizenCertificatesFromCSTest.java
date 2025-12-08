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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesRequest;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdDTO;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.schemas.contract.Personnummer;

@ExtendWith(MockitoExtension.class)
class CitizenCertificatesFromCSTest {


    private static final Personnummer PERSONAL_IDENTITY_NUMBER = Personnummer.createPersonnummer("191212121212").orElseThrow();
    private static final Personnummer COORDINATION_NUMBER = Personnummer.createPersonnummer("191212721212").orElseThrow();
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CitizenCertificateConverter citizenCertificateConverter;
    @InjectMocks
    private CitizenCertificatesFromCS citizenCertificatesFromCS;

    @Test
    void shallReturnEmptyListIfResponseFromCSIsEmpty() {
        final var expectedResult = Collections.emptyList();

        doReturn(Collections.emptyList()).when(csIntegrationService).getCitizenCertificates(
            GetCitizenCertificatesRequest.builder()
                .personId(
                    PersonIdDTO.builder()
                        .id(PERSONAL_IDENTITY_NUMBER.getOriginalPnr())
                        .type(PersonIdTypeDTO.PERSONAL_IDENTITY_NUMBER)
                        .build())
                .build()
        );

        final var actualResult = citizenCertificatesFromCS.get(PERSONAL_IDENTITY_NUMBER);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void shallReturnEmptyListForRequestWithCoordinationNumberIfResponseFromCSIsEmpty() {
        final var expectedResult = Collections.emptyList();

        doReturn(Collections.emptyList()).when(csIntegrationService).getCitizenCertificates(
            GetCitizenCertificatesRequest.builder()
                .personId(
                    PersonIdDTO.builder()
                        .id(COORDINATION_NUMBER.getOriginalPnr())
                        .type(PersonIdTypeDTO.COORDINATION_NUMBER)
                        .build())
                .build()
        );

        final var actualResult = citizenCertificatesFromCS.get(COORDINATION_NUMBER);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void shallReturnListOfCitizenCertificateDTOs() {
        final var citizenCertificateDTO = CitizenCertificateDTO.builder().build();
        final var expectedResult = List.of(citizenCertificateDTO);
        final var certificate = new Certificate();
        final var responseFromCS = List.of(certificate);

        doReturn(responseFromCS).when(csIntegrationService).getCitizenCertificates(
            GetCitizenCertificatesRequest.builder()
                .personId(
                    PersonIdDTO.builder()
                        .id(PERSONAL_IDENTITY_NUMBER.getOriginalPnr())
                        .type(PersonIdTypeDTO.PERSONAL_IDENTITY_NUMBER)
                        .build())
                .build()
        );
        doReturn(citizenCertificateDTO).when(citizenCertificateConverter).convert(certificate);

        final var actualResult = citizenCertificatesFromCS.get(PERSONAL_IDENTITY_NUMBER);
        assertEquals(expectedResult, actualResult);
    }
}
