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

package se.inera.intyg.intygstjanst.web.integration.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v2.MeddelandeReferens;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToRecipientValidatorTest {

    private static final String MEDDELANDE_ID = "meddelandeId";
    private static final String INTYG_ID = "intygsId";
    private static final String PATIENT_CRN = "191212121212";

    @Mock
    private CertificateService certificateService;

    @Mock
    private ArendeRepository messageRepository;

    @InjectMocks
    private SendMessageToRecipientValidator validator;

    @Test
    public void validateTest() throws Exception {
        setupCertificateExist(PATIENT_CRN);
        List<String> validationErrors = validator.validate(buildOkMessage(PATIENT_CRN));
        // should validate ok - i.e., no errors
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void invalidAmneskodTest() throws Exception {
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("invalid-amneskod");
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void paminnelseMeddelandeIdOnOtherThanPaminnTest() throws Exception {
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("AVSTMN");
        message.setPaminnelseMeddelandeId("paminnelseMeddelandeId");
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void paminnelseMeddelandeIdMissingOnPaminnTest() throws Exception {
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("PAMINN");
        message.setPaminnelseMeddelandeId(null);
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void paminnelseMeddelandeExistTest() throws Exception {
        final String paminnelseMeddelandeId = "paminnelseMeddelandeId";
        setupMeddelandeExist(paminnelseMeddelandeId);
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("PAMINN");
        message.setPaminnelseMeddelandeId(paminnelseMeddelandeId);
        List<String> validationErrors = validator.validate(message);
        // should validate ok - i.e., no errors
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void paminnelseMeddelandeDoesNotExistTest() throws Exception {
        final String paminnelseMeddelandeId = "paminnelseMeddelandeId";
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("PAMINN");
        message.setPaminnelseMeddelandeId(paminnelseMeddelandeId);
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void sistaDatumForSvarOnQuestionTest() throws Exception {
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.setSistaDatumForSvar(LocalDate.now().plusDays(7));
        List<String> validationErrors = validator.validate(message);
        // should validate ok - i.e., no errors
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void sistaDatumForSvarOnAnswerTest() throws Exception {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
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
    public void sistaDatumForSvarOnPaminnTest() throws Exception {
        final String paminnelseMeddelandeId = "svarPaMeddelandeId";
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
    public void nonUniqueMeddelandeIdTest() throws Exception {
        setupMeddelandeExist(MEDDELANDE_ID);
        setupCertificateExist(PATIENT_CRN);
        List<String> validationErrors = validator.validate(buildOkMessage(PATIENT_CRN));
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void svarPaMeddelandeExistTest() throws Exception {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        setupMeddelandeExist(svarPaMeddelandeId, "OVRIGT");
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("OVRIGT");
        message.setSvarPa(new MeddelandeReferens());
        message.getSvarPa().setMeddelandeId(svarPaMeddelandeId);
        List<String> validationErrors = validator.validate(message);
        // should validate ok - i.e., no errors
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    public void svarPaMeddelandeDoesNotExistTest() throws Exception {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.setSvarPa(new MeddelandeReferens());
        message.getSvarPa().setMeddelandeId(svarPaMeddelandeId);
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void svarPaAmneNotConsistentTest() throws Exception {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        setupMeddelandeExist(svarPaMeddelandeId, "KOMPLT");
        setupCertificateExist(PATIENT_CRN);
        SendMessageToRecipientType message = buildOkMessage(PATIENT_CRN);
        message.getAmne().setCode("OVRIGT");
        message.setSvarPa(new MeddelandeReferens());
        message.getSvarPa().setMeddelandeId(svarPaMeddelandeId);
        List<String> validationErrors = validator.validate(message);
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void certificateDoesNotExistTest() throws Exception {
        when(certificateService.getCertificateForCare(INTYG_ID)).thenThrow(new InvalidCertificateException("not found", null));
        List<String> validationErrors = validator.validate(buildOkMessage(PATIENT_CRN));
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void patientCrnNotConsistentWithCertificateTest() throws Exception {
        setupCertificateExist("19121212-1212");
        List<String> validationErrors = validator.validate(buildOkMessage("19121212-2558"));
        assertFalse(validationErrors.isEmpty());
    }

    @Test
    public void normalizeCrnBeforeCompareTest() throws Exception {
        setupCertificateExist("19121212-1212");
        List<String> validationErrors = validator.validate(buildOkMessage("191212121212"));
        // should validate ok - i.e., no errors
        assertTrue(validationErrors.isEmpty());
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
        when(certificateService.getCertificateForCare(INTYG_ID)).thenReturn(buildCertificate(patientCrn));
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

    private Certificate buildCertificate(String crn) {
        Certificate certificate = new Certificate();
        certificate.setCivicRegistrationNumber(new Personnummer(crn));
        return certificate;
    }
}
