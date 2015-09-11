package se.inera.certificate.integration;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.logging.HashUtility;
import se.inera.certificate.service.ConsentService;
import se.inera.certificate.service.MonitoringLogService;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.insuranceprocess.healthreporting.setconsent.rivtabp20.v1.SetConsentResponderInterface;
import se.inera.intyg.insuranceprocess.healthreporting.setconsentresponder.v1.SetConsentRequestType;
import se.inera.intyg.insuranceprocess.healthreporting.setconsentresponder.v1.SetConsentResponseType;


@SchemaValidation
public class SetConsentResponderImpl implements SetConsentResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetConsentResponderImpl.class);

    @Autowired
    private ConsentService consentService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public SetConsentResponseType setConsent(AttributedURIType logicalAddress, SetConsentRequestType parameters) {
        SetConsentResponseType response = new SetConsentResponseType();
        try {
            consentService.setConsent(parameters.getPersonnummer(), parameters.isConsentGiven());
            response.setResult(ResultOfCallUtil.okResult());
            if (parameters.isConsentGiven()) {
                monitoringLogService.logConsentGiven(HashUtility.hash(parameters.getPersonnummer()));
            } else {
                monitoringLogService.logConsentRevoked(HashUtility.hash(parameters.getPersonnummer()));
            }
        } catch (DataIntegrityViolationException e) {
            // INTYG-886 GeSamtycke anropas ibland flera gånger i rask takt av klienter, vilket leder till ett
            // race condition som ger DataIntegrityViolationException.
            LOGGER.warn("Consent already given for " + parameters.getPersonnummer() + " - ignored.");
            response.setResult(ResultOfCallUtil.infoResult("Consent already given for " + parameters.getPersonnummer()));
        } catch (HibernateOptimisticLockingFailureException e) {
            // INTYG-886 ÅtertaSamtycke kan teoretiskt anropas flera gånger i rask takt av klienter, vilket leder till ett
            // race condition som ger HibernateOptimisticLockingFailureException.
            LOGGER.warn("Consent already revoked for " + parameters.getPersonnummer() + " - ignored.");
            response.setResult(ResultOfCallUtil.infoResult("Consent already revoked for " + parameters.getPersonnummer()));
        }
        return response;
    }

}
