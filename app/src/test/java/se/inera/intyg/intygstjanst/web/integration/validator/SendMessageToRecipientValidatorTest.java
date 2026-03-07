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
package se.inera.intyg.intygstjanst.web.integration.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.persistence.model.dao.ArendeRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.MeddelandeReferens;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SendMessageToRecipientValidatorTest {

    private static final String MEDDELANDE_ID = "meddelandeId";
    private static final String INTYG_ID = "intygsId";
    private static final String PATIENT_CRN = "191212121212";
    private static final String SVAR_PA_MEDDELANDE_ID = "svarPaMeddelandeId";
    private static final String PAMINNELSE_MEDDELANDE_ID = "paminnelseMeddelandeId";
    private static final String SUBJECT_CODE_OVRIGT = "OVRIGT";

    @Mock
    private CertificateService certificateService;
    @Mock
    private ArendeRepository messageRepository;

    @InjectMocks
    private SendMessageToRecipientValidator validator;

    @Test
    void validateTest() throws Exception {
        setupCertificateExist(PATIENT_CRN);
        List<String> validationErrors = validator.validate(buildOkMessage(PATIENT_CRN));
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    void invalidAmneskodTest() throws Exception {
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("invalid-amneskod");
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    void paminnelseMeddelandeIdOnOtherThanPaminnTest() throws Exception {
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("AVSTMN");
        message.setPaminnelseMeddelandeId(PAMINNELSE_MEDDELANDE_ID);
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    void paminnelseMeddelandeIdMissingOnPaminnTest() throws Exception {
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("PAMINN");
        message.setPaminnelseMeddelandeId(null);
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    void paminnelseMeddelandeExistTest() throws Exception {
        final String paminnelseMeddelandeId = PAMINNELSE_MEDDELANDE_ID;
        setupMeddelandeExist(paminnelseMeddelandeId);
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("PAMINN");
        message.setPaminnelseMeddelandeId(paminnelseMeddelandeId);
        List<String> validationErrors = validator.validate(message);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    void paminnelseMeddelandeDoesNotExistTest() throws Exception {
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("PAMINN");
        message.setPaminnelseMeddelandeId(PAMINNELSE_MEDDELANDE_ID);
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    void sistaDatumForSvarOnQuestionTest() throws Exception {
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.setSistaDatumForSvar(LocalDate.now().plusDays(7));
        List<String> validationErrors = validator.validate(message);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    void sistaDatumForSvarOnAnswerTest() throws Exception {
        final String svarPaMeddelandeId = SVAR_PA_MEDDELANDE_ID;
        setupMeddelandeExist(svarPaMeddelandeId);
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.setSvarPa(new MeddelandeReferens());
        message.getSvarPa().setMeddelandeId(svarPaMeddelandeId);
        message.setSistaDatumForSvar(LocalDate.now().plusDays(7));
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    void sistaDatumForSvarOnPaminnTest() throws Exception {
        final String paminnelseMeddelandeId = SVAR_PA_MEDDELANDE_ID;
        setupMeddelandeExist(paminnelseMeddelandeId);
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("PAMINN");
        message.setPaminnelseMeddelandeId(paminnelseMeddelandeId);
        message.setSistaDatumForSvar(LocalDate.now().plusDays(7));
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    void nonUniqueMeddelandeIdTest() throws Exception {
        setupMeddelandeExist(MEDDELANDE_ID);
        setupCertificateExist(PATIENT_CRN);
        List<String> validationErrors = validator.validate(buildOkMessage(PATIENT_CRN));
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    void svarPaMeddelandeExistTest() throws Exception {
        final String svarPaMeddelandeId = SVAR_PA_MEDDELANDE_ID;
        setupMeddelandeExist(svarPaMeddelandeId, SUBJECT_CODE_OVRIGT);
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode(SUBJECT_CODE_OVRIGT);
        message.setSvarPa(new MeddelandeReferens());
        message.getSvarPa().setMeddelandeId(svarPaMeddelandeId);
        List<String> validationErrors = validator.validate(message);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    void svarPaMeddelandeDoesNotExistTest() throws Exception {
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.setSvarPa(new MeddelandeReferens());
        message.getSvarPa().setMeddelandeId(SVAR_PA_MEDDELANDE_ID);
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    void svarPaAmneNotConsistentTest() throws Exception {
        final String svarPaMeddelandeId = SVAR_PA_MEDDELANDE_ID;
        setupMeddelandeExist(svarPaMeddelandeId, "KOMPLT");
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode(SUBJECT_CODE_OVRIGT);
        message.setSvarPa(new MeddelandeReferens());
        message.getSvarPa().setMeddelandeId(svarPaMeddelandeId);
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    void certificateDoesNotExistTest() throws Exception {
        when(certificateService.getCertificateForCare(INTYG_ID)).thenThrow(new InvalidCertificateException("not found", null));
        assertThrows(InvalidCertificateException.class, () -> validator.validate(buildOkMessage(PATIENT_CRN)));
    }

    @Test
    void patientCrnNotConsistentWithCertificateTest() throws Exception {
        setupCertificateExist("19121212-1212");
        List<String> validationErrors = validator.validate(buildOkMessage("19121212-2558"));
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    void normalizeCrnBeforeCompareTest() throws Exception {
        setupCertificateExist("19121212-1212");
        List<String> validationErrors = validator.validate(buildOkMessage("191212121212"));
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    void shouldContainValidationErrorIfCertificateIsNotSent() throws InvalidCertificateException {
        when(certificateService.getCertificateForCare(INTYG_ID)).thenReturn(buildCertificate(PATIENT_CRN, CertificateState.UNHANDLED));
        List<String> validationErrors = validator.validate(buildOkMessage(PATIENT_CRN));

        assertEquals(1, validationErrors.size());
    }

    @Test
    void shouldContainSpecificValidationErrorIfCertificateIsNotSent() throws InvalidCertificateException {
        when(certificateService.getCertificateForCare(INTYG_ID)).thenReturn(buildCertificate(PATIENT_CRN, CertificateState.UNHANDLED));
        List<String> validationErrors = validator.validate(buildOkMessage(PATIENT_CRN));
        String expectedResult = "Certificate is not sent to recipient";

        assertTrue(validationErrors.contains(expectedResult));
    }

    @Test
    void shouldNotContainValidationErrorIfCertificateIsSent() throws InvalidCertificateException {
        when(certificateService.getCertificateForCare(INTYG_ID)).thenReturn(buildCertificate(PATIENT_CRN, CertificateState.SENT));
        List<String> validationErrors = validator.validate(buildOkMessage(PATIENT_CRN));

        assertTrue(validationErrors.isEmpty());
    }


    @Test
    void csValidateShouldNotReturnValidationErrorForValidMessage() {
        final var validationErrors = new ArrayList<String>();
        validator.csValidate(buildOkMessage(PATIENT_CRN), validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    void csValidateShouldNotCallCertificateService() {
        final var validationErrors = new ArrayList<String>();
        validator.csValidate(buildOkMessage(PATIENT_CRN), validationErrors);
        verifyNoInteractions(certificateService);
    }

    @Test
    void csValidateShouldReturnValidationErrorForInvalidAmneskod() {
        final var validationErrors = new ArrayList<String>();
        final var message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("invalid-amneskod");

        validator.csValidate(message, validationErrors);
        assertEquals(1, validationErrors.size());
        assertTrue(validationErrors.contains("Invalid amneskod"));
    }

    @Test
    void csValidateShouldReturnValidationErrorForNonUniqueMeddelandeId() {
        final var validationErrors = new ArrayList<String>();
        when(messageRepository.findByMeddelandeId(MEDDELANDE_ID)).thenReturn(new Arende());

        validator.csValidate(buildOkMessage(PATIENT_CRN), validationErrors);
        assertEquals(1, validationErrors.size());
        assertTrue(validationErrors.contains("MeddelandeId is not globally unique"));
    }

    @Test
    void csValidateShouldReturnValidationErrorForSistaDatumForSvarOnAnswer() {
        final var validationErrors = new ArrayList<String>();
        final var arende = new Arende();
        arende.setAmne("KOMPLT");

        final var message = buildOkMessage(PATIENT_CRN);
        message.setSvarPa(new MeddelandeReferens());
        message.getSvarPa().setMeddelandeId(SVAR_PA_MEDDELANDE_ID);
        message.setSistaDatumForSvar(LocalDate.now().plusDays(7));

        when(messageRepository.findByMeddelandeId(SVAR_PA_MEDDELANDE_ID)).thenReturn(arende);

        validator.csValidate(message, validationErrors);
        assertEquals(1, validationErrors.size());
        assertTrue(validationErrors.contains("SistaDatumForSvar is only valid on Questions"));
    }

    @Test
    void csValidateShouldReturnValidationErrorForPaminnelseMeddelandeDoesNotExist() {
        final var validationErrors = new ArrayList<String>();
        final var message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("PAMINN");
        message.setPaminnelseMeddelandeId(PAMINNELSE_MEDDELANDE_ID);

        validator.csValidate(message, validationErrors);
        assertEquals(1, validationErrors.size());
        assertTrue(validationErrors.contains("Paminnelse Meddelande does not exist"));
    }

    @Test
    void csValidateShouldReturnValidationErrorForSvarPaAmneNotConsistentTest() {
        final var validationErrors = new ArrayList<String>();
        final var arende = new Arende();
        arende.setAmne("KOMPLT");

        final var message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode(SUBJECT_CODE_OVRIGT);
        message.setSvarPa(new MeddelandeReferens());
        message.getSvarPa().setMeddelandeId(SVAR_PA_MEDDELANDE_ID);

        when(messageRepository.findByMeddelandeId(SVAR_PA_MEDDELANDE_ID)).thenReturn(arende);

        validator.csValidate(message, validationErrors);
        assertEquals(1, validationErrors.size());
        assertTrue(validationErrors.contains("Svar amne is not consistent with question"));
    }

    private void setupMeddelandeExist(String meddelandeId) {
        setupMeddelandeExist(meddelandeId, null);
    }

    private void setupMeddelandeExist(String meddelandeId, String amne) {
        Arende arende = new Arende();
        arende.setAmne(amne);
        when(messageRepository.findByMeddelandeId(meddelandeId)).thenReturn(arende);
    }

    private void setupCertificateExist(String patientCrn) throws InvalidCertificateException {
        when(certificateService.getCertificateForCare(INTYG_ID)).thenReturn(buildCertificate(patientCrn, CertificateState.SENT));
    }

    private SendMessageToRecipientType buildOkMessage(String patientCrn) {
        SendMessageToRecipientType message = new SendMessageToRecipientType();
        message.setMeddelandeId(MEDDELANDE_ID);
        message.setAmne(new Amneskod());
        message.getAmne().setCode("KOMPLT");
        message.setIntygsId(new IntygId());
        message.getIntygsId().setExtension(INTYG_ID);
        message.setPatientPersonId(new PersonId());
        message.getPatientPersonId().setExtension(patientCrn);
        return message;
    }

    private Certificate buildCertificate(String crn, CertificateState state) {
        Certificate certificate = new Certificate();
        certificate.setCivicRegistrationNumber(Personnummer.createPersonnummer(crn).orElseThrow());
        certificate.setStates(List.of(new CertificateStateHistoryEntry("target", state, LocalDateTime.now())));

        return certificate;
    }
}
