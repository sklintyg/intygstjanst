package se.inera.certificate.integration;

import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.integration.module.exception.InvalidCertificateException;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.MonitoringLogService;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatus.rivtabp20.v1.SetCertificateStatusResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusResponseType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;


/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class SetCertificateStatusResponderImpl implements SetCertificateStatusResponderInterface {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public SetCertificateStatusResponseType setCertificateStatus(AttributedURIType logicalAddress, SetCertificateStatusRequestType request) {

        SetCertificateStatusResponseType response = new SetCertificateStatusResponseType();

        try {
            certificateService.setCertificateState(new Personnummer(request.getNationalIdentityNumber()), request.getCertificateId(), request.getTarget(), CertificateState.valueOf(request.getStatus().name()), request.getTimestamp());
            response.setResult(ResultOfCallUtil.okResult());
            monitoringLogService.logCertificateStatusChanged(request.getCertificateId(), request.getStatus() != null ? request.getStatus().name() : null);
        } catch (InvalidCertificateException e) {
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
        }

        return response;
    }
}
