package se.inera.intyg.intygstjanst.web.integration;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.web.service.ConsentService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
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
        final Personnummer civicRegistrationNumber = new Personnummer(parameters.getPersonnummer());
        try {
            consentService.setConsent(civicRegistrationNumber, parameters.isConsentGiven());
            response.setResult(ResultOfCallUtil.okResult());
            if (parameters.isConsentGiven()) {
                monitoringLogService.logConsentGiven(civicRegistrationNumber);
            } else {
                monitoringLogService.logConsentRevoked(civicRegistrationNumber);
            }
        } catch (DataIntegrityViolationException e) {
            // INTYG-886 GeSamtycke anropas ibland flera gånger i rask takt av klienter, vilket leder till ett
            // race condition som ger DataIntegrityViolationException.
            LOGGER.warn("Consent already given for " + civicRegistrationNumber.getPnrHash() + " - ignored.");
            response.setResult(ResultOfCallUtil.infoResult("Consent already given for " + civicRegistrationNumber.getPnrHash()));
        } catch (HibernateOptimisticLockingFailureException e) {
            // INTYG-886 ÅtertaSamtycke kan teoretiskt anropas flera gånger i rask takt av klienter, vilket leder till ett
            // race condition som ger HibernateOptimisticLockingFailureException.
            LOGGER.warn("Consent already revoked for " + civicRegistrationNumber.getPnrHash() + " - ignored.");
            response.setResult(ResultOfCallUtil.infoResult("Consent already revoked for " + civicRegistrationNumber.getPnrHash()));
        }
        return response;
    }

}
