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
package se.inera.certificate.service.impl;

import static se.inera.certificate.modules.support.api.dto.TransportModelVersion.LEGACY_LAKARUTLATANDE;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.exception.MissingModuleException;
import se.inera.certificate.exception.RecipientUnknownException;
import se.inera.certificate.exception.ServerException;
import se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException;
import se.inera.certificate.integration.exception.ResultTypeErrorException;
import se.inera.certificate.integration.module.ModuleApiFactory;
import se.inera.certificate.integration.module.exception.ModuleNotFoundException;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.TransportModelResponse;
import se.inera.certificate.modules.support.api.dto.TransportModelVersion;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.service.CertificateSenderService;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.RecipientService;
import se.inera.certificate.service.recipientservice.Recipient;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;

/**
 * @author andreaskaltenbach
 */
@Service
public class CertificateSenderServiceImpl implements CertificateSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateSenderServiceImpl.class);

    private static final Unmarshaller UNMARSHALLER;

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private ModuleApiFactory moduleApiFactory;

    @Autowired
    @Qualifier("registerMedicalCertificateClient")
    private RegisterMedicalCertificateResponderInterface registerMedicalCertificateQuestionClient;

    @Autowired
    private se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateResponderInterface registerCertificateClient;

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class,
                    se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateType.class);
            UNMARSHALLER = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendCertificate(Certificate certificate, String target) {

        Utlatande utlatande = certificateService.getLakarutlatande(certificate);

        try {
            ModuleEntryPoint module = moduleApiFactory.getModuleEntryPoint(utlatande);

            // Use target from parameter if present, otherwise use the default receiver from the module's entryPoint.
            String logicalAddress;

            if (target == null) {
                logicalAddress = module.getDefaultRecieverLogicalAddress();

            } else {
                Recipient recipient = recipientService.getRecipient(target);
                logicalAddress = recipient.getLogicalAddress();
            }

            TransportModelVersion transportModelVersion = recipientService.getVersion(logicalAddress, module.getModuleId());
            LOGGER.debug(String.format("Getting transport model version from recipientService, got: %s", transportModelVersion));

            TransportModelResponse response = module.getModuleApi().marshall(new ExternalModelHolder(certificate.getDocument()),
                    transportModelVersion);
            invokeReceiverService(response.getTransportModel(), logicalAddress, transportModelVersion);

        } catch (ModuleNotFoundException e) {
            String message = String.format("The module '%s' was not found - not registered in application",
                    certificate.getType());
            LOGGER.error(message);
            throw new MissingModuleException(message, e);

        } catch (ModuleException e) {
            String message = String.format("Failed to unmarshal certificate for certificate type '%s'",
                    certificate.getType());
            LOGGER.error(message);
            throw new ServerException(message, e);

        } catch (RecipientUnknownException e) {
            String message = String.format("Found no matching recipient for logical adress: '%s'", target);
            LOGGER.error(e.getMessage());
            throw new ServerException(message, e);
        }
    }

    private void invokeReceiverService(String xml, String logicalAddress, TransportModelVersion type) {
        try {
            Class<?> unmarshallType = (type.equals(LEGACY_LAKARUTLATANDE) ? RegisterMedicalCertificateType.class
                    : UtlatandeType.class);

            Object request = UNMARSHALLER.unmarshal(
                    new StreamSource(new ByteArrayInputStream(xml.getBytes())), unmarshallType).getValue();
            if (type.equals(LEGACY_LAKARUTLATANDE)) {
                AttributedURIType address = new AttributedURIType();
                address.setValue(logicalAddress);
                RegisterMedicalCertificateResponseType response = registerMedicalCertificateQuestionClient
                        .registerMedicalCertificate(address, (RegisterMedicalCertificateType) request);

                // check whether call was successful or not
                if (response.getResult().getResultCode() != ResultCodeEnum.OK) {
                    throw new ExternalWebServiceCallFailedException(response.getResult());
                }

            } else {
                se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateType req =
                        new se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateType();

                req.setUtlatande((UtlatandeType) request);

                se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateResponseType response = registerCertificateClient
                        .registerMedicalCertificate(logicalAddress, req);

                // check whether call was successful or not
                if (response.getResult().getResultCode() != ResultCodeType.OK) {
                    throw new ResultTypeErrorException(response.getResult());
                }
            }

        } catch (JAXBException e) {
            throw new ServerException(e);
        }
    }

}
