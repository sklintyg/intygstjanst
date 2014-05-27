/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate (http://code.google.com/p/inera-certificate).
 *
 * Inera Certificate is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Certificate is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.certificate.integration;

import static se.inera.certificate.integration.util.ResultTypeUtil.errorResult;
import static se.inera.certificate.integration.util.ResultTypeUtil.okResult;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.exception.ServerException;
import se.inera.certificate.integration.converter.MetaDataResolver;
import se.inera.certificate.integration.module.exception.ModuleNotFoundException;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.support.api.dto.TransportModelVersion;
import se.inera.certificate.modules.support.api.exception.ModuleException;

import com.google.common.base.Throwables;

/**
 * @author andreaskaltenbach
 */
@Transactional
@SchemaValidation
public class GetCertificateForCareResponderImpl extends AbstractGetCertificateResponderImpl implements
        GetCertificateForCareResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCertificateForCareResponderImpl.class);

    private static Unmarshaller unmarshaller;

    @Autowired
    private MetaDataResolver metaDataResolver;

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(UtlatandeType.class);
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new ServerException("Failed to initialize JAXB context required for unmarshaller");
        }
    }

    @Override
    public GetCertificateForCareResponseType getCertificateForCare(String logicalAddress,
            GetCertificateForCareRequestType request) {

        GetCertificateForCareResponseType response = new GetCertificateForCareResponseType();

        CertificateOrResultType certificateOrResultType = getCertificateForCare(request.getCertificateId());

        if (certificateOrResultType.hasError()) {
            ResultType resultType = certificateOrResultType.getResultType();
            response.setResult(resultType);
            return response;
        }

        try {
            Certificate certificate = certificateOrResultType.getCertificate();
            response.setMeta(metaDataResolver.toClinicalProcessCertificateMetaType(certificate));
            attachCertificateDocument(certificate, response);

            if (certificate.isDeletedByCareGiver()) {
                response.setResult(errorResult(ErrorIdType.APPLICATION_ERROR, "Certificate '" + request.getCertificateId()
                    + "' has been deleted by care giver"));
            } else if (certificate.isRevoked()) {
                response.setResult(errorResult(ErrorIdType.REVOKED, "Certificate '" + request.getCertificateId()  + "' has been revoked"));
            } else {
                response.setResult(okResult());
            }

        } catch (ModuleNotFoundException | ModuleException e) {
            response.setResult(errorResult(ErrorIdType.APPLICATION_ERROR, "Module error when processing certificates"));
            LOGGER.error(e.getMessage());
        }

        return response;
    }

    @Override
    protected TransportModelVersion getMarshallVersion() {
        return TransportModelVersion.UTLATANDE_V1;
    }

    protected void attachCertificateDocument(Certificate certificate, GetCertificateForCareResponseType response) {

        String transportModel = getCertificateDocument(certificate);

        UtlatandeType utlatande = null;
        try {
            utlatande = unmarshaller.unmarshal(new StreamSource(new StringReader(transportModel)), UtlatandeType.class).getValue();
        } catch (JAXBException e) {
            LOGGER.error("Failed to unmarshall intyg coming from module " + certificate.getType());
            Throwables.propagate(e);
        }

        response.setCertificate(utlatande);
    }
}
