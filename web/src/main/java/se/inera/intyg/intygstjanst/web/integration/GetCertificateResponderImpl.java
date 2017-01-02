/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Throwables;

import se.inera.intyg.common.fkparent.model.converter.CertificateStateHolderConverter;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleContainerApi;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v1.GetCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v1.GetCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v1.GetCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v2.RegisterCertificateType;

@SchemaValidation
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
                throw new ServerException("Certificate with id " + certificateId + " is deleted from intygstjansten");
            } else {
                setCertificateBody(certificate, response);
            }
        } catch (InvalidCertificateException e) {
            throw new ServerException("Certificate with id " + certificateId + " is invalid or does not exist");
        }
        return response;
    }

    protected void setCertificateBody(CertificateHolder certificate, GetCertificateResponseType response) {
        try {
            RegisterCertificateType jaxbObject = JAXB.unmarshal(new StringReader(certificate.getOriginalCertificate()),
                    RegisterCertificateType.class);
            response.setIntyg(jaxbObject.getIntyg());
            response.getIntyg().getStatus().addAll(CertificateStateHolderConverter.toIntygsStatusType(certificate.getCertificateStates()));
        } catch (Exception e) {
            LOGGER.error("Error while converting in GetCertificate for id: {} with stacktrace: {}", certificate.getId(), e.getStackTrace());
            Throwables.propagate(e);
        }
    }

}
