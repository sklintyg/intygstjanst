/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoType;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateTypeInfo;

/**
 * Created by eriklupander on 2017-05-11.
 */
@SchemaValidation
public class GetCertificateTypeInfoResponderImpl implements GetCertificateTypeInfoResponderInterface {

    @Autowired
    private CertificateService certificateService;

    @Override
    @PrometheusTimeMethod
    public GetCertificateTypeInfoResponseType getCertificateTypeInfo(String logicalAddress, GetCertificateTypeInfoType request) {

        if (isNullOrEmpty(request)) {
            throw new IllegalArgumentException("Request to GetCertificateType is missing required parameter 'intygs-id'");
        }

        final CertificateTypeInfo certificateTypeInfo = certificateService.getCertificateTypeInfo(request.getIntygsId());
        if (certificateTypeInfo == null) {
            throw new ServerException("Failed to get certificate's type. "
                + "Certificate with id " + request.getIntygsId() + " is invalid or does not exist");
        }

        GetCertificateTypeInfoResponseType response = new GetCertificateTypeInfoResponseType();
        response.setTyp(certificateTypeInfo.getTypAvIntyg());
        response.setTypVersion(certificateTypeInfo.getVersion());
        return response;
    }

    private boolean isNullOrEmpty(GetCertificateTypeInfoType request) {
        return request == null || request.getIntygsId() == null || request.getIntygsId().trim().length() == 0;
    }
}
