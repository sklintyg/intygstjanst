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
import org.springframework.stereotype.Service;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException;
import se.inera.certificate.integration.module.ModuleApiFactory;
import se.inera.certificate.integration.module.exception.ModuleNotFoundException;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.TransportModelResponse;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.service.CertificateSenderService;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;

import com.google.common.base.Throwables;

/**
 * @author andreaskaltenbach
 */
@Service
public class CertificateSenderServiceImpl implements CertificateSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateSenderServiceImpl.class);

    private static Unmarshaller UNMARSHALLER;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private ModuleApiFactory moduleApiFactory;

    @Autowired
    private RegisterMedicalCertificateResponderInterface registerMedicalCertificateQuestionClient;

    @Override
    public void sendCertificate(Certificate certificate, String target) {

        Utlatande utlatande = certificateService.getLakarutlatande(certificate);

        try {
            ModuleEntryPoint module = moduleApiFactory.getModuleEntryPoint(utlatande);
            TransportModelResponse response = module.getModuleApi().marshall(new ExternalModelHolder(certificate.getDocument()),
                    LEGACY_LAKARUTLATANDE);

            invokeReceiverService(response.getTransportModel(), module.getDefaultRecieverLogicalAddress());

        } catch (ModuleNotFoundException e) {
            String message = String.format("The module '%s' was not found - not registered in application",
                    certificate.getType());
            LOGGER.error(message);
            // TODO: Throw better exception here?
            throw new RuntimeException(message, e);

        } catch (ModuleException e) {
            String message = String.format("Failed to unmarshal certificate for certificate type '%s'",
                    certificate.getType());
            LOGGER.error(message);
            // TODO: Throw better exception here?
            throw new RuntimeException(message);
        }
    }

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
            UNMARSHALLER = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            Throwables.propagate(e);
        }
    }

    private void invokeReceiverService(String xml, String logicalAddress) {

        try {
            RegisterMedicalCertificateType request = UNMARSHALLER.unmarshal(
                    new StreamSource(new ByteArrayInputStream(xml.getBytes())), RegisterMedicalCertificateType.class)
                    .getValue();
            AttributedURIType address = new AttributedURIType();
            address.setValue(logicalAddress);
            RegisterMedicalCertificateResponseType response = registerMedicalCertificateQuestionClient
                    .registerMedicalCertificate(address, request);

            // check whether call was successful or not
            if (response.getResult().getResultCode() != ResultCodeEnum.OK) {
                throw new ExternalWebServiceCallFailedException(response.getResult());
            }

        } catch (JAXBException e) {
            Throwables.propagate(e);
        }
    }
}
