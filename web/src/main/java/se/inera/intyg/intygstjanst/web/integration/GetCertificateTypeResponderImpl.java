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

import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetype.v1.GetCertificateTypeResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetype.v1.GetCertificateTypeResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetype.v1.GetCertificateTypeType;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;

import static se.inera.intyg.common.support.Constants.KV_INTYGSTYP_CODE_SYSTEM;

/**
 * Created by eriklupander on 2017-05-11.
 */
@SchemaValidation
public class GetCertificateTypeResponderImpl implements GetCertificateTypeResponderInterface {

    @Autowired
    private CertificateService certificateService;

    @Override
    public GetCertificateTypeResponseType getCertificateType(String logicalAddress,
                                                             GetCertificateTypeType request) {

        if (request == null || isNullOrEmpty(request)) {
             throw new IllegalArgumentException("Request to GetCertificateType is missing required parameter 'intygs-id'");
        }

        GetCertificateTypeResponseType response = new GetCertificateTypeResponseType();

        try {
            Certificate cert = certificateService.getCertificateForCare(request.getIntygsId());
            TypAvIntyg typAvIntyg = new TypAvIntyg();
            typAvIntyg.setCode(cert.getType());
            typAvIntyg.setCodeSystem(KV_INTYGSTYP_CODE_SYSTEM);
            response.setTyp(typAvIntyg);
        } catch (InvalidCertificateException e) {
            throw new ServerException("Certificate with id " + request.getIntygsId() + " is invalid or does not exist");
        }
        return response;
    }

    private boolean isNullOrEmpty(GetCertificateTypeType request) {
        return request.getIntygsId() == null || request.getIntygsId().trim().length() == 0;
    }
}
