package se.inera.certificate.integration;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.service.ConsentService;
import se.inera.ifv.insuranceprocess.healthreporting.setconsent.v1.rivtabp20.SetConsentResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.setconsentresponder.v1.SetConsentRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.setconsentresponder.v1.SetConsentResponseType;

@SchemaValidation
public class SetConsentResponderImpl implements SetConsentResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetConsentResponderImpl.class);

    @Autowired
    private ConsentService consentService;

    @Override
    public SetConsentResponseType setConsent(AttributedURIType logicalAddress, SetConsentRequestType parameters) {
        SetConsentResponseType response = new SetConsentResponseType();
        try {
            consentService.setConsent(parameters.getPersonnummer(), parameters.isConsentGiven());
            response.setResult(ResultOfCallUtil.okResult());
            LOGGER.info(LogMarkers.MONITORING, "Consent " + (parameters.isConsentGiven() ? "given" : "revoked") + " for " + parameters.getPersonnummer());
        } catch (DataIntegrityViolationException e) {
            // INTYG-886 GeSamtycke anropas ibland flera gånger i rask takt av klienter, vilket leder till ett
            // race condition som ger DataIntegrityViolationException.
            LOGGER.warn(LogMarkers.MONITORING, "Consent already given for " + parameters.getPersonnummer() + " - ignored.");
            response.setResult(ResultOfCallUtil.infoResult("Consent already given for " + parameters.getPersonnummer()));
        } catch (HibernateOptimisticLockingFailureException e) {
            // INTYG-886 ÅtertaSamtycke kan teoretiskt anropas flera gånger i rask takt av klienter, vilket leder till ett
            // race condition som ger HibernateOptimisticLockingFailureException.
            LOGGER.warn(LogMarkers.MONITORING, "Consent already revoked for " + parameters.getPersonnummer() + " - ignored.");
            response.setResult(ResultOfCallUtil.infoResult("Consent already revoked for " + parameters.getPersonnummer()));
        }
        return response;
    }

}
