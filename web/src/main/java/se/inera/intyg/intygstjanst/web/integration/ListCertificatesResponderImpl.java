/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.listcertificates.rivtabp20.v1.ListCertificatesResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.listcertificatesresponder.v1.ListCertificatesRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.listcertificatesresponder.v1.ListCertificatesResponseType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.converter.ModelConverter;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.integration.converter.ConverterUtil;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * @author andreaskaltenbach
 */
public class ListCertificatesResponderImpl implements ListCertificatesResponderInterface {

    @Autowired
    private CertificateService certificateService;

    @Override
    @PrometheusTimeMethod
    public ListCertificatesResponseType listCertificates(AttributedURIType logicalAddress, ListCertificatesRequestType parameters) {

        ListCertificatesResponseType response = new ListCertificatesResponseType();

        List<Certificate> certificates = certificateService.listCertificatesForCitizen(
            createPnr(parameters.getNationalIdentityNumber()),
            parameters.getCertificateType(),
            parameters.getFromDate(),
            parameters.getToDate());

        for (Certificate certificate : certificates) {
            if (parameters.getCertificateType().isEmpty() || !(certificate.isDeleted() || certificate.isRevoked())) {
                response.getMeta().add(ModelConverter.toCertificateMetaType(ConverterUtil.toCertificateHolder(certificate)));
            }
        }
        response.setResult(ResultOfCallUtil.okResult());

        return response;
    }

    private Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }

}
