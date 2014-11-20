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
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.OK;

import java.io.ByteArrayInputStream;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.exception.ExternalWebServiceCallFailedException;
import se.inera.certificate.exception.MissingModuleException;
import se.inera.certificate.exception.RecipientUnknownException;
import se.inera.certificate.exception.ServerException;
import se.inera.certificate.exception.SubsystemCallException;
import se.inera.certificate.exception.ResultTypeErrorException;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.logging.LogMarkers;
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
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.v1.rivtabp20.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.webcert.medcertqa.v1.Amnetyp;
import se.inera.webcert.medcertqa.v1.InnehallType;
import se.inera.webcert.medcertqa.v1.VardAdresseringsType;

/**
 * @author andreaskaltenbach
 */
@Service
public class CertificateSenderServiceImpl implements CertificateSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateSenderServiceImpl.class);

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    @Qualifier("registerMedicalCertificateClient")
    private RegisterMedicalCertificateResponderInterface registerMedicalCertificateClient;

    @Autowired
    private se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface registerCertificateClient;

    @Autowired
    private SendMedicalCertificateQuestionResponderInterface sendMedicalCertificateQuestionResponderInterface;

    @Autowired
    private RevokeMedicalCertificateResponderInterface revokeMedicalCertificateResponderInterface;

    @Autowired
    @Value("${revokecertificate.address.fk7263}")
    private String sendLogicalAddressText;

    private JAXBContext jaxbContext;

    @PostConstruct
    public void initializeJaxbContext() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class, RegisterCertificateType.class);
    }

    @Override
    public void sendCertificate(Certificate certificate, String target) {
        try {
            ModuleEntryPoint module = moduleRegistry.getModuleEntryPoint(certificate.getType());

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

            Object request = jaxbContext.createUnmarshaller().unmarshal(
                    new StreamSource(new ByteArrayInputStream(xml.getBytes())), unmarshallType).getValue();
            if (type.equals(LEGACY_LAKARUTLATANDE)) {
                AttributedURIType address = new AttributedURIType();
                address.setValue(logicalAddress);
                RegisterMedicalCertificateResponseType response = registerMedicalCertificateClient
                        .registerMedicalCertificate(address, (RegisterMedicalCertificateType) request);

                // check whether call was successful or not
                if (response.getResult().getResultCode() != ResultCodeEnum.OK) {
                    throw new ExternalWebServiceCallFailedException(response.getResult());
                }

            } else {
                RegisterCertificateType req = new RegisterCertificateType();
                req.setUtlatande((UtlatandeType) request);
                RegisterCertificateResponseType response = registerCertificateClient.registerCertificate(logicalAddress, req);

                // check whether call was successful or not
                if (response.getResult().getResultCode() != ResultCodeType.OK) {
                    throw new ResultTypeErrorException(response.getResult());
                }
            }

        } catch (JAXBException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public void sendRevokeCertificateMessage(Certificate certificate, String target, RevokeType revokeData) {
        if (target.equals("FK")) {
            useFKRevokationStrategy(certificate, revokeData);
        } else {
            useDefaultRevokationStrategy(certificate, target, revokeData);
        }
    }

    private void useFKRevokationStrategy(Certificate certificate, RevokeType revokeData) {
        String intygId = certificate.getId();
        String vardref = revokeData.getVardReferensId();
        String meddelande = revokeData.getMeddelande();
        if (meddelande == null || meddelande.isEmpty()) {
            meddelande = "meddelande saknas";
        }

        LocalDateTime signTs = revokeData.getLakarutlatande().getSigneringsTidpunkt();
        LocalDateTime avsantTs = revokeData.getAvsantTidpunkt();
        VardAdresseringsType vardAddress = revokeData.getAdressVard();

        QuestionToFkType question = new QuestionToFkType();
        question.setAmne(Amnetyp.MAKULERING_AV_LAKARINTYG);
        question.setVardReferensId(vardref);
        question.setAvsantTidpunkt(avsantTs);
        question.setAdressVard(vardAddress);

        question.setFraga(new InnehallType());
        question.getFraga().setMeddelandeText(meddelande);
        question.getFraga().setSigneringsTidpunkt(signTs);

        question.setLakarutlatande(revokeData.getLakarutlatande());
        AttributedURIType sendLogicalAddress = new AttributedURIType();
        sendLogicalAddress.setValue(sendLogicalAddressText);
        SendMedicalCertificateQuestionType parameters = new SendMedicalCertificateQuestionType();
        parameters.setQuestion(question);

        SendMedicalCertificateQuestionResponseType sendResponse = sendMedicalCertificateQuestionResponderInterface
                .sendMedicalCertificateQuestion(sendLogicalAddress, parameters);
        if (sendResponse.getResult().getResultCode() != OK) {
            String message = "Failed to send question to Försäkringskassan for revoking certificate '" + intygId
                    + "'. Info from forsakringskassan: " + sendResponse.getResult().getInfoText();
            LOGGER.error(LogMarkers.MONITORING, message);
            throw new SubsystemCallException("FK", message);
        }
    }

    private void useDefaultRevokationStrategy(Certificate certificate, String target, RevokeType revokeData) {
        RevokeMedicalCertificateRequestType request = new RevokeMedicalCertificateRequestType();
        request.setRevoke(revokeData);

        AttributedURIType sendLogicalAddress = new AttributedURIType();
        sendLogicalAddress.setValue(target);

        RevokeMedicalCertificateResponseType sendResponse = revokeMedicalCertificateResponderInterface.revokeMedicalCertificate(sendLogicalAddress,
                request);
        if (sendResponse.getResult().getResultCode() != OK) {
            String message = "Failed to send question to '" + target + "' for revoking certificate '" + certificate.getId()
                    + "'. Info from recipient: " + sendResponse.getResult().getInfoText();
            LOGGER.error(LogMarkers.MONITORING, message);
            throw new SubsystemCallException(target, message);
        }
    }

}
