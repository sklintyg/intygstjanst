/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;

/**
 * Created by eriklupander on 2017-05-11.
 */
@SchemaValidation
public class GetCertificateTypeResponderImpl implements GetCertificateTypeResponderInterface {

    @Autowired
    private CertificateService certificateService;

    @Override
    @PrometheusTimeMethod
    public GetCertificateTypeResponseType getCertificateType(String logicalAddress, GetCertificateTypeType request) {

        if (isNullOrEmpty(request)) {
            throw new IllegalArgumentException("Request to GetCertificateType is missing required parameter 'intygs-id'");
        }

        TypAvIntyg typAvIntyg = certificateService.getCertificateType(request.getIntygsId());
        if (typAvIntyg == null) {
            throw new ServerException("Failed to get certificate's type. "
                    + "Certificate with id " + request.getIntygsId() + " is invalid or does not exist");
        }

        GetCertificateTypeResponseType response = new GetCertificateTypeResponseType();
        response.setTyp(typAvIntyg);
        return response;
    }

    private boolean isNullOrEmpty(GetCertificateTypeType request) {
        return request == null || request.getIntygsId() == null || request.getIntygsId().trim().length() == 0;
    }
}