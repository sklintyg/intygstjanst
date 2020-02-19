/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import java.io.StringReader;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.fkparent.model.converter.CertificateStateHolderConverter;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.ModuleContainerApi;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.integration.util.CertificateStateFilterUtil;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v2.GetCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v2.GetCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v2.GetCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@SchemaValidation
public class GetCertificateResponderImpl implements GetCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCertificateResponderImpl.class);

    @Autowired
    private ModuleContainerApi moduleContainer;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private CertificateService certificateService;

    @Override
    @PrometheusTimeMethod
    public GetCertificateResponseType getCertificate(String logicalAddress, GetCertificateType request) {

        final String certificateId = request.getIntygsId().getExtension();
        final Part part = request.getPart();

        try {
            if (isInvalidPartForTestCertificate(certificateId, part.getCode())) {
                LOGGER.error("Failed to retrieve certificate: '{}' because it is flagged as test certificate and part is set as: {} ",
                    certificateId, part.getCode());
                throw new ServerException("Failed to retrieve certificate: " + certificateId
                    + " because it is flagged as test certificate and part is set as: " + part.getCode());
            }

            CertificateHolder certificate = moduleContainer.getCertificate(certificateId, null, false);
            if (certificate.isDeletedByCareGiver()) {
                throw new ServerException("Certificate with id " + certificateId + " is deleted from intygstjansten");
            } else {
                GetCertificateResponseType response = new GetCertificateResponseType();
                response.setIntyg(convertCertificate(certificate, request.getPart().getCode()));
                moduleContainer.logCertificateRetrieved(certificate.getId(), certificate.getType(), certificate.getCareUnitId(),
                    request.getPart().getCode());
                return response;
            }
        } catch (InvalidCertificateException e) {
            throw new ServerException("Certificate with id " + certificateId + " is invalid or does not exist");
        }
    }

    /**
     * Validate if the certificate is a test certificate and the part asking for the certificate is a receiver of certificates.
     * @param certificateId  the certificates to validate.
     * @param partCode  the part code.
     * @return  true if the part isn't allowed to retrieve test certificates
     */
    private boolean isInvalidPartForTestCertificate(String certificateId, String partCode) throws InvalidCertificateException {
        return certificateService.isTestCertificate(certificateId) && !"HSVARD".equalsIgnoreCase(partCode);
    }

    protected Intyg convertCertificate(CertificateHolder certificateHolder, String part) {
        try {
            RegisterCertificateType jaxbObject =
                JAXB.unmarshal(new StringReader(certificateHolder.getOriginalCertificate()), RegisterCertificateType.class);
            Intyg intyg = jaxbObject.getIntyg();

            // If OriginalCertificate is not a RegisterCertificateType, try to convert it
            if (intyg == null) {
                ModuleApi moduleApi = moduleRegistry.getModuleApi(certificateHolder.getType(), certificateHolder.getTypeVersion());
                Utlatande utlatande = moduleApi.getUtlatandeFromXml(certificateHolder.getOriginalCertificate());
                intyg = moduleApi.getIntygFromUtlatande(utlatande);
            }

            intyg.getStatus()
                .addAll(CertificateStateHolderConverter.toIntygsStatusType(certificateHolder.getCertificateStates().stream()
                    .filter(ch -> CertificateStateFilterUtil.filter(ch, part))
                    .collect(Collectors.toList())));
            return intyg;

        } catch (Exception e) {
            LOGGER.error("Error converting certificate in convertCertificate with id: {}", certificateHolder.getId());
            throw new RuntimeException(e);
        }
    }

}
