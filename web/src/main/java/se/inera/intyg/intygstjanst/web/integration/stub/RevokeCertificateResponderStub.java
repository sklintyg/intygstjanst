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

package se.inera.intyg.intygstjanst.web.integration.stub;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.v2.ResultTypeUtil;
import se.inera.intyg.common.support.stub.MedicalCertificatesStore;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v1.*;

@Transactional
@Component
@SchemaValidation
public class RevokeCertificateResponderStub implements RevokeCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeCertificateResponderStub.class);

    @Autowired
    private MedicalCertificatesStore store;

    @Override
    public RevokeCertificateResponseType revokeCertificate(String logicalAddress, RevokeCertificateType request) {
        RevokeCertificateResponseType response = new RevokeCertificateResponseType();
        String id = request.getIntygsId().getExtension();
        String meddelande = request.getMeddelande();

        LOGGER.info("STUB Received revocation concerning certificate with id: " + id);
        store.makulera(id, meddelande);

        response.setResult(ResultTypeUtil.okResult());
        return response;
    }
}