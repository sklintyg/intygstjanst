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
package se.inera.intyg.intygstjanst.web.integration.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.intygstjanst.web.support.CertificateFactory.CARE_UNIT_ID;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.StatusKod;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.CertificateStateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.ModuleContainerApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v2.GetCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v2.GetCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v2.GetCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@RunWith(MockitoJUnitRunner.class)
public class GetCertificateResponderImplTest {

    protected static final String INTYG_TYPE = "test-type";
    private static final String LOGICAL_ADDRESS = "logicalAddress";
    private static final LocalDateTime TIMESTAMP = LocalDateTime.now();
    private static final String FKASSA_PART_ID = "FKASSA";
    private static final String CITIZEN_PART_ID = "INVANA";
    @Mock
    private ModuleContainerApi moduleContainer;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private CertificateService certificateService;

    @InjectMocks
    private GetCertificateResponderInterface responder = new GetCertificateResponderImpl();

    @Test(expected = ServerException.class)
    public void getTestCertificateAsCertificateReceiver() throws InvalidCertificateException, ModuleNotFoundException, ModuleException {
        final String intygId = "intyg-1";
        when(certificateService.isTestCertificate(any())).thenReturn(true);
        responder.getCertificate(LOGICAL_ADDRESS, createRequest(intygId));
    }

    @Test
    public void getCertificateTest() throws InvalidCertificateException, ModuleNotFoundException, ModuleException {
        final String intygId = "intyg-1";
        when(certificateService.isTestCertificate(any())).thenReturn(false);
        when(moduleContainer.getCertificate(intygId, null, false)).thenReturn(createResponse(intygId, false));
        GetCertificateResponseType res = responder.getCertificate(LOGICAL_ADDRESS, createRequest(intygId));
        assertNotNull(res.getIntyg());
        assertEquals(1, res.getIntyg().getStatus().size());
        assertEquals(StatusKod.SENTTO.name(), res.getIntyg().getStatus().get(0).getStatus().getCode());
        assertEquals(FKASSA_PART_ID, res.getIntyg().getStatus().get(0).getPart().getCode());
        assertEquals(TIMESTAMP, res.getIntyg().getStatus().get(0).getTidpunkt());
        verify(moduleContainer).getCertificate(intygId, null, false);
        verify(moduleContainer).logCertificateRetrieved(intygId, INTYG_TYPE, CARE_UNIT_ID, FKASSA_PART_ID);
    }

    @Test
    public void getCertificateOldFormat() throws InvalidCertificateException, ModuleNotFoundException, ModuleException {
        final String intygId = "intyg-1";
        final CertificateHolder response = createResponse(intygId, false);
        response.setOriginalCertificate("<old></old>");
        when(certificateService.isTestCertificate(any())).thenReturn(false);
        when(moduleContainer.getCertificate(intygId, null, false)).thenReturn(response);
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromXml(or(isNull(), anyString()))).thenReturn(mock(Utlatande.class));
        when(moduleApi.getIntygFromUtlatande(or(isNull(), any(Utlatande.class)))).thenReturn(new Intyg());
        GetCertificateResponseType res = responder.getCertificate(LOGICAL_ADDRESS, createRequest(intygId));
        assertNotNull(res.getIntyg());
        assertEquals(1, res.getIntyg().getStatus().size());
        assertEquals(StatusKod.SENTTO.name(), res.getIntyg().getStatus().get(0).getStatus().getCode());
        assertEquals(FKASSA_PART_ID, res.getIntyg().getStatus().get(0).getPart().getCode());
        assertEquals(TIMESTAMP, res.getIntyg().getStatus().get(0).getTidpunkt());
        verify(moduleContainer).getCertificate(intygId, null, false);
    }

    @Test(expected = ServerException.class)
    public void getCertificateNotFound() throws InvalidCertificateException {
        final String intygId = "intyg-1";
        when(certificateService.isTestCertificate(any())).thenReturn(false);
        when(moduleContainer.getCertificate(intygId, null, false)).thenThrow(new InvalidCertificateException(intygId, null));
        responder.getCertificate(LOGICAL_ADDRESS, createRequest(intygId));
    }

    @Test(expected = ServerException.class)
    public void getCertificateDeletedByCaregiver() throws InvalidCertificateException {
        final String intygId = "intyg-1";
        when(certificateService.isTestCertificate(any())).thenReturn(false);
        when(moduleContainer.getCertificate(intygId, null, false)).thenReturn(createResponse(intygId, true));
        responder.getCertificate(LOGICAL_ADDRESS, createRequest(intygId));
    }

    @Test
    public void statusesAreFilteredDifferentlyForFkAndMinaIntyg() throws Exception {
        // See INTYG-3629
        // Given
        final String intygId = "intyg-1";
        CertificateHolder mockedReturnValue = createResponse(intygId, false,
            new CertificateStateHolder(CITIZEN_PART_ID, CertificateState.DELETED, TIMESTAMP));

        // When
        when(certificateService.isTestCertificate(any())).thenReturn(false);
        when(moduleContainer.getCertificate(intygId, null, false)).thenReturn(mockedReturnValue);
        GetCertificateResponseType fromFk = responder.getCertificate(LOGICAL_ADDRESS,
            createRequest(intygId, FKASSA_PART_ID));
        when(moduleContainer.getCertificate(intygId, null, false)).thenReturn(mockedReturnValue);
        GetCertificateResponseType fromMinaIntyg = responder.getCertificate(LOGICAL_ADDRESS,
            createRequest(intygId, CITIZEN_PART_ID));

        // Then
        assertNotNull(fromFk.getIntyg());
        assertEquals(0, fromFk.getIntyg().getStatus().size());

        assertNotNull(fromMinaIntyg.getIntyg());
        assertEquals(1, fromMinaIntyg.getIntyg().getStatus().size());
        assertEquals(StatusKod.DELETE.name(), fromMinaIntyg.getIntyg().getStatus().get(0).getStatus().getCode());
        assertEquals(CITIZEN_PART_ID, fromMinaIntyg.getIntyg().getStatus().get(0).getPart().getCode());
        assertEquals(TIMESTAMP, fromMinaIntyg.getIntyg().getStatus().get(0).getTidpunkt());
    }

    private GetCertificateType createRequest(String id) {
        return createRequest(id, FKASSA_PART_ID);
    }

    private GetCertificateType createRequest(String id, String part) {
        GetCertificateType parameters = new GetCertificateType();
        parameters.setIntygsId(new IntygId());
        parameters.getIntygsId().setExtension(id);
        Part p = new Part();
        p.setCode(part);
        parameters.setPart(p);
        return parameters;
    }

    private CertificateHolder createResponse(String intygId, boolean deletedByCareGiver) {
        return createResponse(intygId, deletedByCareGiver, new CertificateStateHolder(FKASSA_PART_ID, CertificateState.SENT, TIMESTAMP));
    }

    private CertificateHolder createResponse(String intygId, boolean deletedByCareGiver, CertificateStateHolder... statusItems) {
        CertificateHolder holder = new CertificateHolder();
        holder.setType(INTYG_TYPE);
        holder.setTypeVersion("1.0");
        holder.setId(intygId);
        holder.setDeletedByCareGiver(deletedByCareGiver);
        holder.setOriginalCertificate(
            "<registerCertificateType xmlns:ns2=\"urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificateResponder:3\"><ns2:intyg></ns2:intyg></registerCertificateType>");
        holder.setCertificateStates(Arrays.asList(statusItems));
        holder.setCareUnitId(CARE_UNIT_ID);
        return holder;
    }
}
