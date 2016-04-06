package se.inera.intyg.intygstjanst.web.integration.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.*;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.support.validate.CertificateValidationException;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.integration.util.SendMessageToCareUtil;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToCareValidator.Amneskod;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToCareValidator.ErrorCode;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType.Komplettering;
import se.riv.clinicalprocess.healthcond.certificate.v2.MeddelandeReferens;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToCareValidatorTest {
    private static final String SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML = "SendMessageToCareTest/sendmessagetocare.xml";

    @Mock
    private CertificateService certificateService;

    @Mock
    private Certificate certificate;

    @Mock
    private ArendeRepository sendMessageToCareRepository;

    @InjectMocks
    private SendMessageToCareValidator validator;

    @Test
    public void testValidationOfAmne() {
        List<String> validationErrors = new ArrayList<String>();
        String subject = SendMessageToCareValidator.Amneskod.ARBTID.toString();
        validator.validateMessageSubject(subject, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationOKIfPaminnelseIdIsSpecifiedForPaminnelseSubject() throws Exception {
        List<String> validationErrors = new ArrayList<String>();
        SendMessageToCareType message = buildSendMessageCareType("meddelandeId", Amneskod.PAMINN.toString());
        validator.validatePaminnelseIdConsistency(message, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationOKIfCertificateExistsButCivicRegistrationNumberIsCorrect() throws Exception {
        List<String> validationErrors = new ArrayList<String>();
        SendMessageToCareType message = SendMessageToCareUtil.getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        validateCertificateAndCivicRegistrationNumberConsistency(validationErrors, message.getPatientPersonId().getExtension(), message);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationOKWhenSistaDatumForSvarIsNotSpecifiedForAnswer() throws Exception {
        List<String> validationErrors = new ArrayList<String>();
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
        List<String> validationErrors = new ArrayList<String>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", "KOMPLT");
        sendMessageToCareType.getKomplettering().add(new Komplettering());
        validator.validateConsistencyForKomplettering(sendMessageToCareType, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationOKWhenMessageIsPaminnelseButHasDifferentSubjectThanItsQuestion() throws Exception {
        List<String> validationErrors = new ArrayList<String>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", Amneskod.PAMINN.toString());
        String referencedMeddelandeId = sendMessageToCareType.getSvarPa().getMeddelandeId();
        Arende referencedMessage = buildSendMessageToCare(sendMessageToCareType, referencedMeddelandeId, Amneskod.KOMPLT.toString());

        when(sendMessageToCareRepository.findByMeddelandeId(referencedMeddelandeId)).thenReturn(Arrays.asList(referencedMessage));
        validator.validateConsistencyOfSubject(sendMessageToCareType, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationIsOkWhenAnswerHasSameSubjectAsItsQuestion() throws Exception {
        List<String> validationErrors = new ArrayList<String>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", "OVRIGT");
        String referencedMeddelandeId = sendMessageToCareType.getSvarPa().getMeddelandeId();
        Arende referencedMessage = buildSendMessageToCare(sendMessageToCareType, referencedMeddelandeId, "OVRIGT");
        when(sendMessageToCareRepository.findByMeddelandeId(referencedMeddelandeId)).thenReturn(Arrays.asList(referencedMessage));
        validator.validateConsistencyOfSubject(sendMessageToCareType, validationErrors);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void testThatValidationOkWhenEverythingIsPerfect() throws Exception {
        List<String> validationErrors = new ArrayList<String>();

        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", Amneskod.ARBTID.toString());
        sendMessageToCareType.setSistaDatumForSvar(null);
        sendMessageToCareType.setPaminnelseMeddelandeId(null);
        String referencedMeddelandeId = sendMessageToCareType.getSvarPa().getMeddelandeId();
        Arende referencedMessage = buildSendMessageToCare(sendMessageToCareType, referencedMeddelandeId, Amneskod.ARBTID.toString());
        when(sendMessageToCareRepository.findByMeddelandeId(referencedMeddelandeId)).thenReturn(Arrays.asList(referencedMessage));

        String certificateId = sendMessageToCareType.getIntygsId().getExtension();
        String civicRegistrationNumber = sendMessageToCareType.getPatientPersonId().getExtension();
        Certificate certificate = new Certificate(certificateId, null);
        certificate.setCivicRegistrationNumber(new Personnummer(civicRegistrationNumber));
        when(certificateService.getCertificateForCare(certificateId)).thenReturn(certificate);

        validator.validateSendMessageToCare(sendMessageToCareType);
        assertTrue(validationErrors.isEmpty());
    }

    // ----------- ERROR CASES------------------------------------------------------------------------------

    @Test
    public void testValidationOfAmneProducesError() {
        List<String> validationErrors = new ArrayList<String>();
        String subject = "SomeRandomSubject";
        validator.validateMessageSubject(subject, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.SUBJECT_NOT_SUPPORTED_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsIfPaminnelseIdIsSpecifiedForOtherMessageTypes() throws Exception {
        List<String> validationErrors = new ArrayList<String>();
        SendMessageToCareType message = buildSendMessageCareType("meddelandeId", Amneskod.OVRIGT.toString());
        validator.validatePaminnelseIdConsistency(message, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.PAMINNELSE_ID_INCONSISTENCY_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsWhenPaminnelseIdMissingForPaminnelse() throws Exception {
        List<String> validationErrors = new ArrayList<String>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", Amneskod.PAMINN.toString());
        sendMessageToCareType.setPaminnelseMeddelandeId(null);
        validator.validatePaminnelseIdConsistency(sendMessageToCareType, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.PAMINNELSE_ID_INCONSISTENCY_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsIfCertificateDoesNotExist() throws Exception {
        List<String> validationErrors = new ArrayList<String>();
        SendMessageToCareType message = SendMessageToCareUtil.getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        String certificateId = message.getIntygsId().getExtension();
        String civicRegistrationNumber = message.getPatientPersonId().getExtension();
        when(certificateService.getCertificateForCare(certificateId)).thenReturn(null);
        validator.validateThatCertificateExists(certificateId, civicRegistrationNumber, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.CERTIFICATE_NOT_FOUND_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsIfCertificateExistsButCivicRegistrationNumberIsWrong() throws Exception {
        List<String> validationErrors = new ArrayList<String>();
        SendMessageToCareType message = SendMessageToCareUtil.getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        validateCertificateAndCivicRegistrationNumberConsistency(validationErrors, "101010-1010", message);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.CIVIC_REGISTRATION_NUMBER_INCONSISTENCY_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsWhen_SvarPa_And_SistaDatumForSvar_AreSimultaneouslySpecified() throws Exception {
        List<String> validationErrors = new ArrayList<String>();
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
        List<String> validationErrors = new ArrayList<String>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", "OVRIGT");
        String referencedMeddelandeId = sendMessageToCareType.getSvarPa().getMeddelandeId();
        Arende referencedMessage = buildSendMessageToCare(sendMessageToCareType, referencedMeddelandeId, "KOMPLT");

        when(sendMessageToCareRepository.findByMeddelandeId(referencedMeddelandeId)).thenReturn(Arrays.asList(referencedMessage));
        validator.validateConsistencyOfSubject(sendMessageToCareType, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(SendMessageToCareValidator.ErrorCode.SUBJECT_CONSISTENCY_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsWhenReferencedMessageNotFound() throws Exception {
        List<String> validationErrors = new ArrayList<String>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", "OVRIGT");
        String referencedMeddelandeId = sendMessageToCareType.getSvarPa().getMeddelandeId();
        when(sendMessageToCareRepository.findByMeddelandeId(referencedMeddelandeId)).thenReturn(null);
        validator.validateConsistencyOfSubject(sendMessageToCareType, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.REFERENCED_MESSAGE_NOT_FOUND_ERROR.toString()));
    }

    @Test
    public void testThatValidationFailsForKompletteringInconsistency() throws Exception {
        List<String> validationErrors = new ArrayList<String>();
        SendMessageToCareType sendMessageToCareType = buildSendMessageCareType("originalMessageId", "OVRIGT");
        sendMessageToCareType.getKomplettering().add(new Komplettering());
        validator.validateConsistencyForKomplettering(sendMessageToCareType, validationErrors);
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.get(0).contains(ErrorCode.KOMPLETTERING_INCONSISTENCY_ERROR.toString()));
    }

    @Test(expected = CertificateValidationException.class)
    public void testThatValidationExceptionIsThrown() throws Exception {
        SendMessageToCareType sendMessageToCareType = SendMessageToCareUtil
                .getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        validator.validateSendMessageToCare(sendMessageToCareType);
    }

    private void validateCertificateAndCivicRegistrationNumberConsistency(List<String> validationErrors, String civicRegNumber,
            SendMessageToCareType message)
                    throws Exception, InvalidCertificateException {
        String certificateId = message.getIntygsId().getExtension();
        String civicRegistrationNumber = message.getPatientPersonId().getExtension();
        Certificate certificate = new Certificate(certificateId, null);
        certificate.setCivicRegistrationNumber(new Personnummer(civicRegNumber));
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
        sendMessageToCareType.setAmne(new se.riv.clinicalprocess.healthcond.certificate.types.v2.Amneskod());
        sendMessageToCareType.getAmne().setCode(amne);
        return sendMessageToCareType;
    }

}
