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

package se.inera.intyg.intygstjanst.web.integration;

import java.io.StringReader;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.modules.fkparent.integration.ResultUtil;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleContainerApi;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v1.GetCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v1.GetCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v1.GetCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v2.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;

import com.google.common.base.Throwables;

public class GetCertificateResponderImpl implements GetCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCertificateResponderImpl.class);

    @Autowired
    private ModuleContainerApi moduleContainer;

    @Override
    public GetCertificateResponseType getCertificate(String logicalAddress, GetCertificateType request) {
        GetCertificateResponseType response = new GetCertificateResponseType();

        String certificateId = request.getIntygsId().getExtension();

        try {
            CertificateHolder certificate = moduleContainer.getCertificate(certificateId, null, false);
            if (certificate.isDeletedByCareGiver()) {
                response.setResult(ResultUtil.errorResult(ErrorIdType.APPLICATION_ERROR,
                        String.format("Certificate '%s' has been deleted by care giver", certificateId)));
            } else {
                setCertificateBody(certificate, response);
                if (certificate.isRevoked()) {
                    response.setResult(ResultUtil.errorResult(ErrorIdType.REVOKED,
                            String.format("Certificate '%s' has been revoked", certificateId)));
                } else {
                    response.setResult(ResultUtil.okResult());
                }
            }
        } catch (InvalidCertificateException e) {
            response.setResult(ResultUtil.errorResult(ErrorIdType.VALIDATION_ERROR, e.getMessage()));
        }
        return response;
    }

    protected void setCertificateBody(CertificateHolder certificate, GetCertificateResponseType response) {
        try {
            RegisterCertificateType jaxbObject = JAXB.unmarshal(new StringReader(certificate.getOriginalCertificate()), RegisterCertificateType.class);
            response.setIntyg(jaxbObject.getIntyg());
        } catch (Exception e) {
            LOGGER.error("Error while converting in getMedicalCertificate for id: {} with stacktrace: {}", certificate.getId(), e.getStackTrace());
            Throwables.propagate(e);
        }
    }

}
