/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.common.enumerations.PartKod;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.StatusKod;
import se.inera.intyg.common.support.modules.support.api.*;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;

@RunWith(MockitoJUnitRunner.class)
public class GetCertificateResponderImplTest {

    private static final String LOGICAL_ADDRESS = "logicalAddress";
    private static final LocalDateTime TIMESTAMP = LocalDateTime.now();

    @Mock
    private ModuleContainerApi moduleContainer;

    @InjectMocks
    private GetCertificateResponderInterface responder = new GetCertificateResponderImpl();

    @Test
    public void getCertificateTest() throws InvalidCertificateException {
        final String intygId = "intyg-1";
        when(moduleContainer.getCertificate(intygId, null, false)).thenReturn(createResponse(false));
        GetCertificateResponseType res = responder.getCertificate(LOGICAL_ADDRESS, createRequest(intygId));
        assertNotNull(res.getIntyg());
        assertEquals(1, res.getIntyg().getStatus().size());
        assertEquals(StatusKod.SENTTO.name(), res.getIntyg().getStatus().get(0).getStatus().getCode());
        assertEquals(PartKod.FKASSA.name(), res.getIntyg().getStatus().get(0).getPart().getCode());
        assertEquals(TIMESTAMP, res.getIntyg().getStatus().get(0).getTidpunkt());
        verify(moduleContainer).getCertificate(intygId, null, false);
    }

    @Test(expected = ServerException.class)
    public void getCertificateNotFound() throws InvalidCertificateException {
        final String intygId = "intyg-1";
        when(moduleContainer.getCertificate(intygId, null, false)).thenThrow(new InvalidCertificateException(intygId, null));
        responder.getCertificate(LOGICAL_ADDRESS, createRequest(intygId));
    }

    @Test(expected = ServerException.class)
    public void getCertificateDeletedByCaregiver() throws InvalidCertificateException {
        final String intygId = "intyg-1";
        when(moduleContainer.getCertificate(intygId, null, false)).thenReturn(createResponse(true));
        responder.getCertificate(LOGICAL_ADDRESS, createRequest(intygId));
    }

    private GetCertificateType createRequest(String id) {
        GetCertificateType parameters = new GetCertificateType();
        parameters.setIntygsId(new IntygId());
        parameters.getIntygsId().setExtension(id);
        return parameters;
    }

    private CertificateHolder createResponse(boolean deletedByCareGiver) {
        CertificateHolder holder = new CertificateHolder();
        holder.setDeletedByCareGiver(deletedByCareGiver);
        holder.setOriginalCertificate("<registerCertificateType xmlns:ns2=\"urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificateResponder:2\"><ns2:intyg></ns2:intyg></registerCertificateType>");
        holder.setCertificateStates(Arrays.asList(new CertificateStateHolder("FK", CertificateState.SENT, TIMESTAMP)));
        return holder;
    }
}
