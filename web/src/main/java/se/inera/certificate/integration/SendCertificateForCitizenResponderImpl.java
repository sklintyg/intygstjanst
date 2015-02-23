package se.inera.certificate.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.clinicalprocess.healthcond.certificate.sendcertificateforcitizen.v1.SendCertificateForCitizenResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.sendcertificateforcitizen.v1.SendCertificateForCitizenResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.sendcertificateforcitizen.v1.SendCertificateForCitizenType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.inera.certificate.exception.RecipientUnknownException;
import se.inera.certificate.integration.module.exception.CertificateRevokedException;
import se.inera.certificate.integration.module.exception.InvalidCertificateException;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.service.CertificateService;

public class SendCertificateForCitizenResponderImpl implements SendCertificateForCitizenResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendCertificateForCitizenResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Override
    public SendCertificateForCitizenResponseType sendCertificateForCitizen(String logicalAddress, SendCertificateForCitizenType request) {

        SendCertificateForCitizenResponseType response = new SendCertificateForCitizenResponseType();
        ResultType resultType = new ResultType();

        try {
            // 1. Skicka certifikat till mottagaren
            CertificateService.SendStatus sendStatus = certificateService.sendCertificate(request.getPersonId(), request.getUtlatandeId(), request.getMottagareId());

            String msg = "";
            if (sendStatus == CertificateService.SendStatus.ALREADY_SENT) {
                msg = String.format("Certificate '%s' already sent to '%s'.", request.getUtlatandeId(), request.getMottagareId());
                response.setResult(ResultTypeUtil.infoResult(msg));
                LOGGER.info(LogMarkers.MONITORING, msg);
            } else {
                msg = String.format("Certificate '%s' sent to '%s'.", request.getUtlatandeId(), request.getMottagareId());
                response.setResult(ResultTypeUtil.okResult());
                LOGGER.info(LogMarkers.MONITORING, msg);
            }


        } catch (InvalidCertificateException ex) {
            // return ERROR if no such certificate does exist
            LOGGER.error(LogMarkers.MONITORING, String.format("Certificate '%s' does not exist for user '%s'.", new Object[] { request.getUtlatandeId(), request.getPersonId() }));
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, String.format("Unknown certificate ID: %s", request.getUtlatandeId())));
        } catch (CertificateRevokedException ex) {
            // return INFO if certificate is revoked
            String msg = String.format("Certificate '%s' has been revoked.", request.getUtlatandeId());
            LOGGER.info(LogMarkers.MONITORING, msg);
            response.setResult(ResultTypeUtil.infoResult(msg));
        } catch (RecipientUnknownException e) {
            // return ERROR if recipient is unknwon
            String msg = String.format("Unknown recipient ID: %s", request.getMottagareId());
            LOGGER.error(LogMarkers.MONITORING, msg);
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, msg));
        }

        return response;
    }

}
