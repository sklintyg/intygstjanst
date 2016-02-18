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

import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Throwables;

import se.inera.certificate.modules.fkparent.integration.ResultUtil;
import se.inera.intyg.common.support.integration.module.exception.CertificateAlreadyExistsException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.converter.util.ConverterException;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.ModuleContainerApi;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateXmlResponse;
import se.inera.intyg.common.util.logging.LogMarkers;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v2.ObjectFactory;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v2.RegisterCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v2.RegisterCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v2.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;

public class RegisterCertificateResponderImpl implements RegisterCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterCertificateResponderImpl.class);

    private ObjectFactory objectFactory;
    private JAXBContext jaxbContext;

    @Autowired
    private ModuleContainerApi moduleContainer;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @PostConstruct
    public void initializeJaxbContext() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(RegisterCertificateType.class);
        objectFactory = new ObjectFactory();
    }

    @Override
    public RegisterCertificateResponseType registerCertificate(String logicalAddress, RegisterCertificateType registerCertificate) {
        try {
            String intygsTyp = getIntygsTyp(registerCertificate);
            ModuleApi api = moduleRegistry.getModuleApi(intygsTyp);
            String xml = xmlToString(registerCertificate);
            ValidateXmlResponse validationResponse = api.validateXml(xml);

            if (!validationResponse.hasErrorMessages()) {
                return makeOkResult(registerCertificate, intygsTyp, api, xml);
            } else {
                String validationErrors = String.join(";", validationResponse.getValidationErrors());
                return makeValidationErrorResult(validationErrors);
            }
        } catch (CertificateAlreadyExistsException e) {
            return makeCertificateAlreadyExistsResult(registerCertificate);
        } catch (ConverterException e) {
            return makeValidationErrorResult(e.getMessage());
        } catch (JAXBException e) {
            LOGGER.error("JAXB error in Webservice: ", e);
            Throwables.propagate(e);
        } catch (NotImplementedException e) {
            LOGGER.error("This webservice is not valid for the current certificate type {}", registerCertificate.getIntyg());
            Throwables.propagate(e);
        } catch (Exception e) {
            LOGGER.error("Error in Webservice: ", e);
            Throwables.propagate(e);
        }
        throw new RuntimeException("Unrecoverable exception in registerCertificate");
    }

    private RegisterCertificateResponseType makeOkResult(RegisterCertificateType registerCertificate, String intygsTyp, ModuleApi api,
            String xml) throws Exception, CertificateAlreadyExistsException, InvalidCertificateException {
        RegisterCertificateResponseType response = new RegisterCertificateResponseType();
        Utlatande utlatande = api.getUtlatandeFromIntyg(registerCertificate.getIntyg(), xml);
        CertificateHolder certificateHolder = toCertificateHolder(utlatande, xml, intygsTyp);
        certificateHolder.setOriginalCertificate(xml);
        moduleContainer.certificateReceived(certificateHolder);
        response.setResult(ResultUtil.okResult());
        return response;
    }

    private RegisterCertificateResponseType makeValidationErrorResult(String errorString) {
        RegisterCertificateResponseType response = new RegisterCertificateResponseType();
        response.setResult(ResultUtil.errorResult(ErrorIdType.VALIDATION_ERROR, errorString));
        LOGGER.error(LogMarkers.VALIDATION, errorString);
        return response;
    }

    private RegisterCertificateResponseType makeCertificateAlreadyExistsResult(RegisterCertificateType registerCertificate) {
        RegisterCertificateResponseType response = new RegisterCertificateResponseType();
        response.setResult(ResultUtil.infoResult("Certificate already exists"));
        String certificateId = registerCertificate.getIntyg().getIntygsId().getExtension();
        String issuedBy = registerCertificate.getIntyg().getSkapadAv().getEnhet().getEnhetsId().getExtension();
        LOGGER.warn(LogMarkers.VALIDATION, "Validation warning for intyg " + certificateId + " issued by " + issuedBy
                + ": Certificate already exists - ignored.");
        return response;
    }

    private String xmlToString(RegisterCertificateType registerCertificate) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        JAXBElement<RegisterCertificateType> requestElement = objectFactory.createRegisterCertificate(registerCertificate);
        jaxbContext.createMarshaller().marshal(requestElement, stringWriter);
        return stringWriter.toString();
    }

    private String getIntygsTyp(RegisterCertificateType certificateType) {
        return certificateType.getIntyg().getTyp().getCode().toLowerCase();
    }

    private CertificateHolder toCertificateHolder(Utlatande utlatande, String document, String type) {
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId(utlatande.getId());
        certificateHolder.setCareUnitId(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
        certificateHolder.setCareUnitName(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());
        certificateHolder.setCareGiverId(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid());
        certificateHolder.setSigningDoctorName(utlatande.getGrundData().getSkapadAv().getFullstandigtNamn());
        certificateHolder.setCivicRegistrationNumber(utlatande.getGrundData().getPatient().getPersonId());
        certificateHolder.setSignedDate(utlatande.getGrundData().getSigneringsdatum());
        certificateHolder.setType(type);
        certificateHolder.setDocument(document);
        return certificateHolder;
    }

}
