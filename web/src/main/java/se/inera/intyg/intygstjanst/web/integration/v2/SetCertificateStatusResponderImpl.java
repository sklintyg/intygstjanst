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

package se.inera.intyg.intygstjanst.web.integration.v2;

import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.fkparent.support.ResultTypeUtil;
import se.inera.intyg.common.support.common.enumerations.PartKod;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.StatusKod;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.setCertificateStatus.v1.SetCertificateStatusResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.setCertificateStatus.v1.SetCertificateStatusResponseType;
import se.riv.clinicalprocess.healthcond.certificate.setCertificateStatus.v1.SetCertificateStatusType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;

@SchemaValidation
public class SetCertificateStatusResponderImpl implements SetCertificateStatusResponderInterface {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public SetCertificateStatusResponseType setCertificateStatus(String logicalAddress, SetCertificateStatusType parameters) {
        SetCertificateStatusResponseType response = new SetCertificateStatusResponseType();

        try {
            String certificateId = parameters.getIntygsId().getExtension();
            String target = PartKod.valueOf(parameters.getPart().getCode()).getValue();
            CertificateState certificateState = StatusKod.valueOf(parameters.getStatus().getCode()).toCertificateState();
            certificateService.setCertificateState(certificateId, target, certificateState, parameters.getTidpunkt());
            response.setResult(ResultTypeUtil.okResult());
            monitoringLogService.logCertificateStatusChanged(certificateId, certificateState.name());
        } catch (IllegalArgumentException | InvalidCertificateException e) {
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.VALIDATION_ERROR, e.getMessage()));
        }

        return response;
    }
}
