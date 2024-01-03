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
package se.inera.intyg.intygstjanst.web.integration.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.persistence.model.dao.ArendeRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.integration.util.SendMessageToCareUtil;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToCareValidator.Amneskod;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToCareValidator.ErrorCode;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.Komplettering;
import se.riv.clinicalprocess.healthcond.certificate.v3.MeddelandeReferens;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToCareValidatorTest {

    private static final String SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML =
        "SendMessageToCareTest/sendmessagetocare.xml";
    private static final Recipient FKASSA =
        new RecipientBuilder()
            .setLogicalAddress("FKORG")
            .setName("Föräkringskassa")
            .setId("FKASSA")
            .setCertificateTypes("fk7263")
            .setActive(true)
            .setTrusted(true)
            .build();
    @Mock
    private RecipientService recipientService;
    @Mock
    private CertificateService certificateService;
    @Mock
    private Certificate certificate;
    @Mock
    private ArendeRepository arendeRepository;
    @InjectMocks
    private SendMessageToCareValidator validator;

    @Test
    public void testValidationOfAmne() {
        List<String> validationErrors = new ArrayList<>();
        String subject = SendMessageToCareValidator.Amneskod.AVSTMN.toString();
        validator.validateMessageSubject(subject, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationOKIfPartCodeIsValid() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        validator.validateSkickatAv("FKASSA", validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationOKIfPaminnelseIdIsSpecifiedForPaminnelseSubject() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType message = buildSendMessageCareType("meddelandeId", Amneskod.PAMINN.toString());
        when(arendeRepository.findByMeddelandeId(eq(message.getPaminnelseMeddelandeId()))).thenReturn(new Arende());
        validator.validatePaminnelse(message, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationFailsIfPaminnelseArendeNotExist() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType message = buildSendMessageCareType("meddelandeId", Amneskod.PAMINN.toString());
        validator.validatePaminnelse(message, validationErrors);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationOKIfCertificateExistsButCivicRegistrationNumberIsCorrect() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType message = SendMessageToCareUtil
            .getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        validateCertificateAndCivicRegistrationNumberConsistency(validationErrors, message.getPatientPersonId().getExtension(), message);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationOKWhenSistaDatumForSvarIsNotSpecifiedForAnswer() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType answerWithoutSistaDatumForSvar = SendMessageToCareUtil
            .getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        answerWithoutSistaDatumForSvar.setSvarPa(new MeddelandeReferens());
        answerWithoutSistaDatumForSvar.setSistaDatumForSvar(null);

        SendMessageToCareType questionWithSistaDatumForSvar = SendMessageToCareUtil
            .getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        questionWithSistaDatumForSvar.setSistaDatumForSvar(LocalDate.now());
        questionWithSistaDatumForSvar.setSvarPa(null);

        validator.validateConsistencyForQuestionVsAnswer(answerWithoutSistaDatumForSvar, validationErrors);
        validator.validateConsistencyForQuestionVsAnswer(questionWithSistaDatumForSvar, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationOkForKompletteringConsistency() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", "KOMPLT");
        sendMessageToCareType.getKomplettering().add(new Komplettering());
        validator.validateConsistencyForKomplettering(sendMessageToCareType, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationOKWhenMessageIsPaminnelseButHasDifferentSubjectThanItsQuestion() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", Amneskod.PAMINN.toString());
        String referencedMeddelandeId = sendMessageToCareType.getSvarPa().getMeddelandeId();
        Arende referencedMessage = buildSendMessageToCare(sendMessageToCareType, referencedMeddelandeId, Amneskod.KOMPLT.toString());

        when(arendeRepository.findByMeddelandeId(referencedMeddelandeId)).thenReturn(referencedMessage);
        validator.validateConsistencyOfSubject(sendMessageToCareType, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationIsOkWhenAnswerHasSameSubjectAsItsQuestion() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", "OVRIGT");
        String referencedMeddelandeId = sendMessageToCareType.getSvarPa().getMeddelandeId();
        Arende referencedMessage = buildSendMessageToCare(sendMessageToCareType, referencedMeddelandeId, "OVRIGT");
        when(arendeRepository.findByMeddelandeId(referencedMeddelandeId)).thenReturn(referencedMessage);
        validator.validateConsistencyOfSubject(sendMessageToCareType, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationOkWhenEverythingIsPerfect() throws Exception {
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", Amneskod.AVSTMN.toString());
        sendMessageToCareType.setMeddelandeId("meddelande-id");
        sendMessageToCareType.setSistaDatumForSvar(null);
        sendMessageToCareType.setPaminnelseMeddelandeId(null);
        String referencedMeddelandeId = sendMessageToCareType.getSvarPa().getMeddelandeId();
        Arende referencedMessage = buildSendMessageToCare(sendMessageToCareType, referencedMeddelandeId, Amneskod.AVSTMN.toString());
        when(arendeRepository.findByMeddelandeId(referencedMeddelandeId)).thenReturn(referencedMessage);

        String certificateId = sendMessageToCareType.getIntygsId().getExtension();
        String civicRegistrationNumber = sendMessageToCareType.getPatientPersonId().getExtension();
        Certificate certificate = new Certificate(certificateId);
        certificate.setCivicRegistrationNumber(createPnr(civicRegistrationNumber));
        when(certificateService.getCertificateForCare(certificateId)).thenReturn(certificate);

        List<String> res = validator.validateSendMessageToCare(sendMessageToCareType);
        assertTrue(String.format("Validation errors: %s", res), res.isEmpty());
    }

    @Test
    public void testThatValidationFailsIfPartCodeIsInvalid() throws Exception {
        when(recipientService.getRecipient(eq("FK"))).thenThrow(new RecipientUnknownException("Unknown recipient"));
        List<String> validationErrors = new ArrayList<>();
        validator.validateSkickatAv("FK", validationErrors);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void testValidationOfAmneProducesError() {
        List<String> validationErrors = new ArrayList<>();
        String subject = "SomeRandomSubject";
        validator.validateMessageSubject(subject, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.SUBJECT_NOT_SUPPORTED_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsIfPaminnelseIdIsSpecifiedForOtherMessageTypes() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType message = buildSendMessageCareType("meddelandeId", Amneskod.OVRIGT.toString());
        validator.validatePaminnelse(message, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.PAMINNELSE_ID_INCONSISTENCY_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsWhenPaminnelseIdMissingForPaminnelse() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", Amneskod.PAMINN.toString());
        sendMessageToCareType.setPaminnelseMeddelandeId(null);
        validator.validatePaminnelse(sendMessageToCareType, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.PAMINNELSE_ID_INCONSISTENCY_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsIfCertificateDoesNotExist() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType message = SendMessageToCareUtil
            .getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        String certificateId = message.getIntygsId().getExtension();
        String civicRegistrationNumber = message.getPatientPersonId().getExtension();
        when(certificateService.getCertificateForCare(certificateId)).thenReturn(null);
        validator.validateThatCertificateExists(certificateId, civicRegistrationNumber, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.CERTIFICATE_NOT_FOUND_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsIfCertificateExistsButCivicRegistrationNumberIsWrong() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType message = SendMessageToCareUtil
            .getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        validateCertificateAndCivicRegistrationNumberConsistency(validationErrors, "101010-1010", message);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.CIVIC_REGISTRATION_NUMBER_INCONSISTENCY_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsWhen_SvarPa_And_SistaDatumForSvar_AreSimultaneouslySpecified() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType sendMessageToCareType = SendMessageToCareUtil
            .getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        sendMessageToCareType.setSvarPa(new MeddelandeReferens());
        sendMessageToCareType.setSistaDatumForSvar(LocalDate.now());
        validator.validateConsistencyForQuestionVsAnswer(sendMessageToCareType, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.MESSAGE_TYPE_CONSISTENCY_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsWhenAnswerHasDifferentSubjectThanItsQuestion() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", "OVRIGT");
        String referencedMeddelandeId = sendMessageToCareType.getSvarPa().getMeddelandeId();
        Arende referencedMessage = buildSendMessageToCare(sendMessageToCareType, referencedMeddelandeId, "KOMPLT");

        when(arendeRepository.findByMeddelandeId(referencedMeddelandeId)).thenReturn(referencedMessage);
        validator.validateConsistencyOfSubject(sendMessageToCareType, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(SendMessageToCareValidator.ErrorCode.SUBJECT_CONSISTENCY_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsWhenReferencedMessageNotFound() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", "OVRIGT");
        String referencedMeddelandeId = sendMessageToCareType.getSvarPa().getMeddelandeId();
        when(arendeRepository.findByMeddelandeId(referencedMeddelandeId)).thenReturn(null);
        validator.validateConsistencyOfSubject(sendMessageToCareType, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.REFERENCED_MESSAGE_NOT_FOUND_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsForKompletteringInconsistency() throws Exception {
        List<String> validationErrors = new ArrayList<>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", "OVRIGT");
        sendMessageToCareType.getKomplettering().add(new Komplettering());
        validator.validateConsistencyForKomplettering(sendMessageToCareType, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.KOMPLETTERING_INCONSISTENCY_ERROR.toString()));
    }

    @Test
    public void testThatValidationExceptionIsThrown() throws Exception {
        SendMessageToCareType sendMessageToCareType = SendMessageToCareUtil
            .getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        List<String> res = validator.validateSendMessageToCare(sendMessageToCareType);
        assertFalse(res.isEmpty());
    }

    @Test
    public void testValidationOfEmptyMeddelandeId() {
        List<String> validationErrors = new ArrayList<>();
        validator.validateMeddelandeId("", validationErrors);
        assertFalse(validationErrors.isEmpty());

    }

    @Test
    public void testValidationOfNullMeddelandeId() {
        List<String> validationErrors = new ArrayList<>();
        validator.validateMeddelandeId(null, validationErrors);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void testValidationOfExistingMeddelandeId() {
        final String meddelandeId = "meddelande-id";
        List<String> validationErrors = new ArrayList<>();
        when(arendeRepository.findByMeddelandeId(eq(meddelandeId))).thenReturn(new Arende());
        validator.validateMeddelandeId(meddelandeId, validationErrors);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void testValidationOfMeddelandeId() {
        final String meddelandeId = "meddelande-id";
        List<String> validationErrors = new ArrayList<>();
        when(arendeRepository.findByMeddelandeId(eq(meddelandeId))).thenReturn(null);
        validator.validateMeddelandeId(meddelandeId, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testValidationOfTestCertificateTrue() throws Exception {
        final String certificateId = "certificate-id";
        List<String> validationErrors = new ArrayList<>();
        when(certificateService.isTestCertificate(eq(certificateId))).thenReturn(true);
        validator.validateTestCertificate(certificateId, validationErrors);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void testValidationOfTestCertificateFalse() throws Exception {
        final String certificateId = "certificate-id";
        List<String> validationErrors = new ArrayList<>();
        when(certificateService.isTestCertificate(eq(certificateId))).thenReturn(false);
        validator.validateTestCertificate(certificateId, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    private void validateCertificateAndCivicRegistrationNumberConsistency(List<String> validationErrors, String civicRegNumber,
        SendMessageToCareType message)
        throws Exception, InvalidCertificateException {
        String certificateId = message.getIntygsId().getExtension();
        String civicRegistrationNumber = message.getPatientPersonId().getExtension();
        Certificate certificate = new Certificate(certificateId);
        certificate.setCivicRegistrationNumber(createPnr(civicRegNumber));
        when(certificateService.getCertificateForCare(certificateId)).thenReturn(certificate);
        validator.validateThatCertificateExists(certificateId, civicRegistrationNumber, validationErrors);
    }

    private Arende buildSendMessageToCare(SendMessageToCareType sendMessageToCareType, String referencedMeddelandeId, String amne) {
        Arende referencedMessage = new Arende();
        referencedMessage.setIntygsId(sendMessageToCareType.getIntygsId().getExtension());
        referencedMessage.setAmne(amne);
        referencedMessage.setMeddelandeId(referencedMeddelandeId);
        return referencedMessage;
    }

    private SendMessageToCareType buildSendMessageCareType(String meddelandeId, String amne) throws Exception {
        SendMessageToCareType sendMessageToCareType = SendMessageToCareUtil
            .getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        MeddelandeReferens meddelandeReferens = new MeddelandeReferens();
        meddelandeReferens.setMeddelandeId(meddelandeId);
        sendMessageToCareType.setSvarPa(meddelandeReferens);
        sendMessageToCareType.setAmne(new se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod());
        sendMessageToCareType.getAmne().setCode(amne);
        return sendMessageToCareType;
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }

}
