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
package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.integration.module.exception.CertificateAlreadyExistsException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.ModuleContainerApi;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateXmlResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.intygstjanst.web.integration.util.CertificateHolderConverter;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare;

@RunWith(MockitoJUnitRunner.class)
public class RegisterCertificateResponderImplTest {

    private static final String LOGICAL_ADDRESS = "logicalAddress";
    private static final String INTYGSTYP = "intygTyp";
    private static final String INTYGSVERSION = "11.0";

    @Mock
    private ModuleContainerApi moduleContainer;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private IntygTextsService textsService;

    @Mock
    private PUService puService;

    @Spy
    private CertificateHolderConverter certificateHolderConverter = new CertificateHolderConverter();

    @InjectMocks
    private RegisterCertificateResponderImpl responder = new RegisterCertificateResponderImpl();

    @Before
    public void setUp() throws Exception {
        final Person person = new Person(null, false, false, "", "", "", "", "", "", false);
        final PersonSvar personSvar = PersonSvar.found(person);
        when(puService.getPerson(any())).thenReturn(personSvar);
        when(moduleRegistry.getModuleApi(INTYGSTYP.toLowerCase(), INTYGSVERSION)).thenReturn(moduleApi);
        when(textsService.isVersionSupported(INTYGSTYP.toLowerCase(), INTYGSVERSION)).thenReturn(true);
        when(moduleRegistry.getModuleIdFromExternalId(INTYGSTYP)).thenReturn(INTYGSTYP.toLowerCase());
        when(moduleApi.validateXml(anyString())).thenReturn(new ValidateXmlResponse(ValidationStatus.VALID, new ArrayList<>()));
        responder.initializeJaxbContext();
    }

    @Test
    public void registerCertificateTest() throws Exception {
        final String intygId = "intygId";
        final String enhetId = "enhetId";
        final String enhetNamn = "enhetNamn";
        final String vardgivareId = "vardgivareId";
        final String skapadAvNamn = "skapadAvNamn";
        final String patientId = "191212121212";
        final LocalDateTime signeringstidpunkt = LocalDateTime.now();

        RegisterCertificateResponseType res = responder.registerCertificate(LOGICAL_ADDRESS,
            createRequest(intygId, enhetId, enhetNamn, vardgivareId, skapadAvNamn, patientId, signeringstidpunkt));
        assertNotNull(res);
        assertEquals(ResultCodeType.OK, res.getResult().getResultCode());

        verify(moduleApi).validateXml(anyString());
        ArgumentCaptor<CertificateHolder> certificateHolderCaptor = ArgumentCaptor.forClass(CertificateHolder.class);
        verify(moduleContainer).certificateReceived(certificateHolderCaptor.capture());
        assertEquals(intygId, certificateHolderCaptor.getValue().getId());
        assertEquals(enhetId, certificateHolderCaptor.getValue().getCareUnitId());
        assertEquals(enhetNamn, certificateHolderCaptor.getValue().getCareUnitName());
        assertEquals(vardgivareId, certificateHolderCaptor.getValue().getCareGiverId());
        assertEquals(skapadAvNamn, certificateHolderCaptor.getValue().getSigningDoctorName());
        assertEquals(createPnr(patientId), certificateHolderCaptor.getValue().getCivicRegistrationNumber());
        assertEquals(signeringstidpunkt, certificateHolderCaptor.getValue().getSignedDate());
        assertEquals(INTYGSTYP.toLowerCase(), certificateHolderCaptor.getValue().getType());
        assertNotNull(certificateHolderCaptor.getValue().getOriginalCertificate());
    }

    @Test
    public void registerCertificateValidationErrorsTest() throws Exception {
        when(moduleApi.validateXml(anyString())).thenReturn(new ValidateXmlResponse(ValidationStatus.INVALID, Arrays.asList("fel")));
        RegisterCertificateResponseType res = responder.registerCertificate(LOGICAL_ADDRESS,
            createRequest("intygId", "enhetId", "enhetNamn", "vardgivareId", "skapadAvNamn", "19350108-1234", LocalDateTime.now()));
        assertNotNull(res);
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());
        assertEquals(ErrorIdType.VALIDATION_ERROR, res.getResult().getErrorId());
        assertNotNull(res.getResult().getResultText());

        verify(moduleApi).validateXml(anyString());
        verify(moduleContainer, never()).certificateReceived(any(CertificateHolder.class));
    }

    @Test
    public void registerCertificateCertificateAlreadyExistsTest() throws Exception {
        doThrow(new CertificateAlreadyExistsException("intygId")).when(moduleContainer).certificateReceived(any(CertificateHolder.class));
        RegisterCertificateResponseType res = responder.registerCertificate(LOGICAL_ADDRESS,
            createRequest("intygId", "enhetId", "enhetNamn", "vardgivareId", "skapadAvNamn", "19350108-1234", LocalDateTime.now()));
        assertNotNull(res);
        assertEquals(ResultCodeType.INFO, res.getResult().getResultCode());
        assertEquals("Certificate already exists", res.getResult().getResultText());

        verify(moduleApi).validateXml(anyString());
        verify(moduleContainer).certificateReceived(any(CertificateHolder.class));
    }

    @Test
    public void registerCertificateInvalidCertificateExceptionTest() throws Exception {
        doThrow(new InvalidCertificateException("intygId", null)).when(moduleContainer).certificateReceived(any(CertificateHolder.class));
        RegisterCertificateResponseType res = responder.registerCertificate(LOGICAL_ADDRESS,
            createRequest("intygId", "enhetId", "enhetNamn", "vardgivareId", "skapadAvNamn", "19350108-1234", LocalDateTime.now()));
        assertNotNull(res);
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());
        assertEquals(ErrorIdType.VALIDATION_ERROR, res.getResult().getErrorId());
        assertNotNull(res.getResult().getResultText());

        verify(moduleApi).validateXml(anyString());
        verify(moduleContainer).certificateReceived(any(CertificateHolder.class));
    }

    @Test
    public void registerCertificateInvalidPersonnummer() throws Exception {
        RegisterCertificateResponseType res = responder.registerCertificate(LOGICAL_ADDRESS,
            createRequest("intygId", "enhetId", "enhetNamn", "vardgivareId", "skapadAvNamn", "felaktigt personnummer",
                LocalDateTime.now()));
        assertNotNull(res);
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());
        assertEquals(ErrorIdType.VALIDATION_ERROR, res.getResult().getErrorId());
        assertEquals("Social security number is not of correct format.", res.getResult().getResultText());
    }

    @Test
    public void registerCertificatePatientNotExists() throws Exception {
        final String personId = "19300807-7723";
        when(puService.getPerson(Personnummer.createPersonnummer(personId).get())).thenReturn(PersonSvar.notFound());
        RegisterCertificateResponseType res = responder.registerCertificate(LOGICAL_ADDRESS,
            createRequest("intygId", "enhetId", "enhetNamn", "vardgivareId", "skapadAvNamn", "19300807-7723", LocalDateTime.now()));
        assertNotNull(res);
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());
        assertEquals(ErrorIdType.VALIDATION_ERROR, res.getResult().getErrorId());
        assertEquals("No person exists in PU Service with the social security number", res.getResult().getResultText());
    }

    @Test
    public void registerCertificatePUServiceError() throws Exception {
        final String personId = "20121212-1212";
        when(puService.getPerson(Personnummer.createPersonnummer(personId).get())).thenReturn(PersonSvar.error());
        RegisterCertificateResponseType res = responder.registerCertificate(LOGICAL_ADDRESS,
            createRequest("intygId", "enhetId", "enhetNamn", "vardgivareId", "skapadAvNamn", "20121212-1212", LocalDateTime.now()));
        assertNotNull(res);
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());
        assertEquals(ErrorIdType.TECHNICAL_ERROR, res.getResult().getErrorId());
        assertEquals("Error calling PU Service to validate social security number", res.getResult().getResultText());
    }

    @Test
    public void registerCertificateJaxbExceptionTest() throws Exception {
        JAXBContext jaxbContextMock = mock(JAXBContext.class);
        Field field = RegisterCertificateResponderImpl.class.getDeclaredField("jaxbContext");
        field.setAccessible(true);
        field.set(responder, jaxbContextMock);
        when(jaxbContextMock.createMarshaller()).thenThrow(new JAXBException(""));

        try {
            responder.registerCertificate(LOGICAL_ADDRESS,
                createRequest("intygId", "enhetId", "enhetNamn", "vardgivareId", "skapadAvNamn", "19350108-1234", LocalDateTime.now()));
            fail("should throw");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof JAXBException);
            verify(moduleApi, never()).validateXml(anyString());
            verify(moduleContainer, never()).certificateReceived(any(CertificateHolder.class));
        }
    }

    @Test
    public void registerCertificateWrongCertificateTypeTest() throws Exception {
        when(moduleApi.validateXml(anyString())).thenThrow(new UnsupportedOperationException());

        try {
            responder.registerCertificate(LOGICAL_ADDRESS,
                createRequest("intygId", "enhetId", "enhetNamn", "vardgivareId", "skapadAvNamn", "19350108-1234", LocalDateTime.now()));
            fail("should throw");
        } catch (UnsupportedOperationException e) {
            verify(moduleApi).validateXml(anyString());
            verify(moduleContainer, never()).certificateReceived(any(CertificateHolder.class));
        }
    }

    @Test
    public void registerCertificateOtherExceptionTest() throws Exception {
        doThrow(new RuntimeException("intygId")).when(moduleContainer).certificateReceived(any(CertificateHolder.class));
        try {
            responder.registerCertificate(LOGICAL_ADDRESS,
                createRequest("intygId", "enhetId", "enhetNamn",
                    "vardgivareId", "skapadAvNamn", "19350108-1234", LocalDateTime.now()));
            fail("should throw");
        } catch (RuntimeException e) {
            verify(moduleApi).validateXml(anyString());
            verify(moduleContainer).certificateReceived(any(CertificateHolder.class));
        }
    }

    private RegisterCertificateType createRequest(String intygId, String enhetId, String enhetNamn,
        String vardgivareId, String skapadAvNamn, String patientId,
        LocalDateTime signeringstidpunkt) {

        RegisterCertificateType parameters = new RegisterCertificateType();
        parameters.setIntyg(new Intyg());
        parameters.getIntyg().setTyp(new TypAvIntyg());
        parameters.getIntyg().getTyp().setCode(INTYGSTYP);
        parameters.getIntyg().setVersion(INTYGSVERSION);
        parameters.getIntyg().setIntygsId(new IntygId());
        parameters.getIntyg().getIntygsId().setExtension(intygId);
        parameters.getIntyg().setSkapadAv(new HosPersonal());
        parameters.getIntyg().getSkapadAv().setFullstandigtNamn(skapadAvNamn);
        parameters.getIntyg().getSkapadAv().setPersonalId(new HsaId());
        parameters.getIntyg().getSkapadAv().setEnhet(new Enhet());
        parameters.getIntyg().getSkapadAv().getEnhet().setEnhetsnamn(enhetNamn);
        parameters.getIntyg().getSkapadAv().getEnhet().setEnhetsId(new HsaId());
        parameters.getIntyg().getSkapadAv().getEnhet().getEnhetsId().setExtension(enhetId);
        parameters.getIntyg().getSkapadAv().getEnhet().setVardgivare(new Vardgivare());
        parameters.getIntyg().getSkapadAv().getEnhet().getVardgivare().setVardgivareId(new HsaId());
        parameters.getIntyg().getSkapadAv().getEnhet().getVardgivare().getVardgivareId().setExtension(vardgivareId);
        parameters.getIntyg().setPatient(new Patient());
        parameters.getIntyg().getPatient().setPersonId(new PersonId());
        parameters.getIntyg().getPatient().getPersonId().setExtension(patientId);
        parameters.getIntyg().setSigneringstidpunkt(signeringstidpunkt);
        return parameters;
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }

}
