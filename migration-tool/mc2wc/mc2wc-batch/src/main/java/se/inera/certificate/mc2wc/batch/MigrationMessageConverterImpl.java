package se.inera.certificate.mc2wc.batch;

import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import se.inera.certificate.mc2wc.jpa.model.AddressCare;
import se.inera.certificate.mc2wc.jpa.model.Answer;
import se.inera.certificate.mc2wc.jpa.model.Certificate;
import se.inera.certificate.mc2wc.jpa.model.Patient;
import se.inera.certificate.mc2wc.jpa.model.Question;
import se.inera.certificate.mc2wc.jpa.model.State;
import se.inera.certificate.mc2wc.jpa.model.Subject;
import se.inera.certificate.mc2wc.message.AnswerType;
import se.inera.certificate.mc2wc.message.CareGiverType;
import se.inera.certificate.mc2wc.message.CarePersonType;
import se.inera.certificate.mc2wc.message.CareUnitType;
import se.inera.certificate.mc2wc.message.CertificateType;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.PatientType;
import se.inera.certificate.mc2wc.message.QuestionType;
import se.inera.certificate.mc2wc.message.QuestionOriginatorType;
import se.inera.certificate.mc2wc.message.QuestionSubjectType;
import se.inera.certificate.mc2wc.message.StatusType;

public class MigrationMessageConverterImpl {

    public MigrationMessageConverterImpl() {

    }

    public MigrationMessage toMigrationMessage(Certificate mcCert, boolean migrateCert) {

        MigrationMessage msg = new MigrationMessage();
        
        if (migrateCert) {
            CertificateType wcCert = toWCCertificate(mcCert);
            msg.setCertificate(wcCert);
        }
        
        Set<Question> questions = mcCert.getQuestions();

        for (Question mcQuestion : questions) {
            QuestionType wcQuestionAnswer = toWCQuestionAnswer(mcCert.getId(), mcQuestion);
            msg.getQuestions().add(wcQuestionAnswer);
        }

        return msg;
    }

    private CertificateType toWCCertificate(Certificate mcCert) {
        CertificateType wcCert = new CertificateType();
        
        wcCert.setCertificateId(mcCert.getId());
        wcCert.setContents(mcCert.getDocument());
        
        PatientType wcPatient = new PatientType();
        wcPatient.setFullName(mcCert.getPatientName());
        wcPatient.setPersonId(mcCert.getPatientSsn());
        wcCert.setPatient(wcPatient);
        
        return wcCert;
    }

    private QuestionType toWCQuestionAnswer(String certificateId, Question mcQuestion) {

        QuestionType qa = new QuestionType();

        qa.setCertificateId(certificateId);
        qa.setExternalReference(mcQuestion.getFkReferenceId());

        qa.setQuestionLastAnswerDate(LocalDate.fromDateFields(mcQuestion.getLastDateForAnswer()));
        qa.setSent(LocalDateTime.fromDateFields(mcQuestion.getSentAt()));
        qa.setSigned(LocalDateTime.fromDateFields(mcQuestion.getTextSignedAt()));

        qa.setOriginator(toQuestionOriginatorType(mcQuestion.getOriginator()));
        qa.setStatus(toStatusType(mcQuestion.getState()));
        qa.setSubject(toQuestionSubject(mcQuestion.getSubject()));
        
        qa.setQuestionText(mcQuestion.getText());
        qa.setCaption(mcQuestion.getCaption());

        qa.setPatient(toPatient(mcQuestion.getPatient()));
        qa.setCarePerson(toCarePerson(mcQuestion.getAddressCare()));

        qa.setAnswer(toAnswer(mcQuestion.getAnswer()));

        return qa;
    }

    private CarePersonType toCarePerson(AddressCare addressCare) {

        CarePersonType cp = new CarePersonType();

        cp.setFullName(addressCare.getCarePersonName());
        cp.setPersonId(addressCare.getCarePersonId());
        cp.setPrescriptionCode(addressCare.getCarePersonCode());
        cp.setCareUnit(toCareUnit(addressCare));

        return cp;
    }

    private CareUnitType toCareUnit(AddressCare addressCare) {

        CareUnitType cu = new CareUnitType();

        cu.setId(addressCare.getCareUnitId());
        cu.setName(addressCare.getCareUnitName());
        cu.setWorkplaceCode(addressCare.getCareUnitWorkplaceCode());
        cu.setPhone(addressCare.getPhoneNumber());
        cu.setEmail(addressCare.getEmailAddress());
        cu.setPostalAddress(addressCare.getPostalAddress());
        cu.setPostalNumber(addressCare.getPostalNumber());
        cu.setPostalCity(addressCare.getPostalCity());

        CareGiverType cg = new CareGiverType();
        cg.setId(addressCare.getCareGiverId());
        cg.setName(addressCare.getCareGiverName());

        cu.setCareGiver(cg);

        return cu;
    }

    private PatientType toPatient(Patient patient) {

        PatientType pat = new PatientType();

        pat.setFullName(patient.getName());
        pat.setPersonId(patient.getSsn());

        return pat;
    }

    private AnswerType toAnswer(Answer answer) {

        if (answer == null) {
            return null;
        }
        
        AnswerType answerType = new AnswerType();
        
        answerType.setText(answer.getText());
        answerType.setStatus(toStatusType(answer.getState()));
        answerType.setSigned(LocalDateTime.fromDateFields(answer.getTextSignedAt()));
        answerType.setSent(LocalDateTime.fromDateFields(answer.getSentAt()));
        
        return answerType;
    }

    private QuestionSubjectType toQuestionSubject(Subject subject) {

        switch (subject) {
        case CONTACT:
            return QuestionSubjectType.CONTACT;
        case KOMPLEMENTING:
            return QuestionSubjectType.KOMPLEMENTING;
        case MAKULERING:
            return QuestionSubjectType.MAKULERING;
        case MEETING:
            return QuestionSubjectType.MEETING ;
        case REMINDER:
            return QuestionSubjectType.REMINDER;
        case WORK_PROLONGING:
            return QuestionSubjectType.WORK_PROLONGING;
        case OTHER:
            return QuestionSubjectType.OTHER;
        default:
            return null;
        }
    }

    private StatusType toStatusType(State state) {
        
        switch (state) {
        case CREATED:
            return StatusType.CREATED;
        case EDITED:
            return StatusType.EDITED;
        case PRINTED:
            return StatusType.PRINTED;
        case SENT:
            return StatusType.SENT;
        case SIGNED:
            return StatusType.SIGNED;
        case SIGNED_AND_SENT:
            return StatusType.SIGNED_AND_SENT;
        case SENT_HANDLED:
            return StatusType.SENT_HANDLED;
        case SENT_UNHANDLED:
            return StatusType.SENT_UNHANDLED;
        default:
            return null;
        }
    }

    private QuestionOriginatorType toQuestionOriginatorType(String source) {

        if (source.equalsIgnoreCase("FK")) {
            return QuestionOriginatorType.FK;
        } else if (source.equalsIgnoreCase("CARE")) {
            return QuestionOriginatorType.CARE;
        }

        return null;
    }
}
