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


import java.util.List;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.certificate.v1.CertificateStatusType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.integration.module.exception.MissingConsentException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.insuranceprocess.healthreporting.getcertificatecontent.rivtabp20.v1.GetCertificateContentResponderInterface;
import se.inera.intyg.insuranceprocess.healthreporting.getcertificatecontentresponder.v1.GetCertificateContentRequestType;
import se.inera.intyg.insuranceprocess.healthreporting.getcertificatecontentresponder.v1.GetCertificateContentResponseType;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.integration.util.CertificateStateHistoryEntryConverter;
import se.inera.intyg.intygstjanst.web.service.CertificateService;


/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class GetCertificateContentResponderImpl implements GetCertificateContentResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCertificateContentResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Override
    public GetCertificateContentResponseType getCertificateContent(AttributedURIType logicalAddress,
            GetCertificateContentRequestType request) {

        GetCertificateContentResponseType response = new GetCertificateContentResponseType();

        Certificate certificate;
        final Personnummer civicRegistrationNumber = new Personnummer(request.getNationalIdentityNumber());
        try {
            certificate = certificateService.getCertificateForCitizen(civicRegistrationNumber, request.getCertificateId());
        } catch (MissingConsentException ex) {
            LOGGER.info("Tried to get certificate '" + request.getCertificateId() + "' but user '"
                    + civicRegistrationNumber.getPnrHash() + "' has not given consent.");
            response.setResult(ResultOfCallUtil.failResult(String.format("Missing consent for patient %s",
                    civicRegistrationNumber.getPnrHash())));
            return response;
        } catch (InvalidCertificateException ex) {
            // return ERROR if no such certificate does exist
            LOGGER.info("Tried to get certificate '" + request.getCertificateId() + "' but no such certificate does exist for user '" + civicRegistrationNumber.getPnrHash() + "'.");
            response.setResult(ResultOfCallUtil.failResult(String.format("Unknown certificate ID: %s", request.getCertificateId())));
            return response;
        } catch (CertificateRevokedException ex) {
            // return INFO if certificate is revoked
            LOGGER.info("Tried to get certificate '" + request.getCertificateId() + "' but certificate has been revoked'.");
            response.setResult(ResultOfCallUtil.infoResult("Certificate '" + request.getCertificateId() + "' has been revoked"));
            return response;
        }

        attachCertificateDocument(certificate, response);
        response.setResult(ResultOfCallUtil.okResult());
        return response;

    }

    private void attachCertificateDocument(Certificate certificate, GetCertificateContentResponseType response) {
        // extract certificate states from certificate meta data
        List<CertificateStatusType> states = CertificateStateHistoryEntryConverter.toCertificateStatusType(certificate
                .getStates());

        response.setType(certificate.getType());
        response.setCertificate(certificate.getDocument());
        response.getStatuses().addAll(states);
    }
}