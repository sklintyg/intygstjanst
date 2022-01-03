/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3._2000._09.xmldsig_.SignatureType;
import org.w3._2002._06.xmldsig_filter2.XPathType;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.common.support.integration.module.exception.CertificateAlreadyExistsException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.ModuleContainerApi;
import se.inera.intyg.common.support.modules.support.api.dto.AdditionalMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateRelation;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.model.PersonSvar.Status;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.monitoring.logging.LogMarkers;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.ObjectFactory;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.DatePeriodType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PQType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Relation;

@SchemaValidation
public class RegisterCertificateResponderImpl implements RegisterCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterCertificateResponderImpl.class);

    private ObjectFactory objectFactory;
    private JAXBContext jaxbContext;

    @Autowired
    private ModuleContainerApi moduleContainer;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private IntygTextsService textsService;

    @Autowired
    private PUService puService;

    @PostConstruct
    public void initializeJaxbContext() throws JAXBException {
        // We need to register DatePeriodType with the JAXBContext explicitly for some reason.
        jaxbContext = JAXBContext.newInstance(RegisterCertificateType.class, DatePeriodType.class, SignatureType.class,
            XPathType.class, PartialDateType.class, PQType.class);
        objectFactory = new ObjectFactory();
    }

    @Override
    @PrometheusTimeMethod
    public RegisterCertificateResponseType registerCertificate(String logicalAddress, RegisterCertificateType registerCertificate) {
        try {
            final String intygsTyp = getIntygsTyp(registerCertificate);
            final String version = registerCertificate.getIntyg().getVersion();

            // Major version validation
            ModuleApi api = moduleRegistry.getModuleApi(intygsTyp, version);

            // Minor version validation
            if (!textsService.isVersionSupported(intygsTyp, version)) {
                return makeInvalidCertificateVersionResult(registerCertificate);
            }

            final Optional<RegisterCertificateResponseType> validatePersonError =
                validatePersonInPU(registerCertificate.getIntyg().getPatient().getPersonId().getExtension());
            if (validatePersonError.isPresent()) {
                return validatePersonError.get();
            }

            final var xml = xmlToString(registerCertificate);
            final var validationResponse = api.validateXml(xml);
            final var additionalInfo = api.getAdditionalInfo(registerCertificate.getIntyg());
            final var additionalMetaData = api.getAdditionalMetaData(registerCertificate.getIntyg()).orElse(null);

            if (!validationResponse.hasErrorMessages()) {
                return storeIntyg(registerCertificate, intygsTyp, xml, additionalInfo, additionalMetaData);
            } else {
                String validationErrors = String.join(";", validationResponse.getValidationErrors());
                return makeValidationErrorResult(validationErrors);
            }
        } catch (CertificateAlreadyExistsException e) {
            return makeCertificateAlreadyExistsResult(registerCertificate);
        } catch (ModuleNotFoundException e) {
            return makeInvalidCertificateVersionResult(registerCertificate);
        } catch (InvalidCertificateException e) {
            return makeInvalidCertificateResult(registerCertificate);
        } catch (JAXBException e) {
            LOGGER.error("JAXB error in Webservice: ", e);
            throw new RuntimeException(e);
        } catch (UnsupportedOperationException e) {
            LOGGER.error("This webservice is not valid for the current certificate type {}", registerCertificate.getIntyg());
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unrecoverable exception in registerCertificate: ", e);
            throw new RuntimeException(e);
        }
    }

    private RegisterCertificateResponseType storeIntyg(
        final RegisterCertificateType registerCertificate,
        final String intygsTyp,
        final String xml, final String additionalInfo, AdditionalMetaData additionalMetaData)
        throws CertificateAlreadyExistsException, InvalidCertificateException {
        RegisterCertificateResponseType response = new RegisterCertificateResponseType();
        CertificateHolder certificateHolder = toCertificateHolder(registerCertificate.getIntyg(), intygsTyp, xml, additionalInfo,
            additionalMetaData);
        moduleContainer.certificateReceived(certificateHolder);
        response.setResult(ResultTypeUtil.okResult());
        return response;
    }

    private RegisterCertificateResponseType makeValidationErrorResult(String errorString) {
        RegisterCertificateResponseType response = new RegisterCertificateResponseType();
        response.setResult(ResultTypeUtil.errorResult(ErrorIdType.VALIDATION_ERROR, errorString));
        LOGGER.error(LogMarkers.VALIDATION, errorString);
        return response;
    }

    private RegisterCertificateResponseType makeCertificateAlreadyExistsResult(RegisterCertificateType registerCertificate) {
        RegisterCertificateResponseType response = new RegisterCertificateResponseType();
        // NOTE: Do NOT change this string as we are dependent on comparing this in FkParentModuleApi
        response.setResult(ResultTypeUtil.infoResult("Certificate already exists"));
        String certificateId = registerCertificate.getIntyg().getIntygsId().getExtension();
        String issuedBy = registerCertificate.getIntyg().getSkapadAv().getEnhet().getEnhetsId().getExtension();
        LOGGER.warn(LogMarkers.VALIDATION, "Validation warning for intyg " + certificateId + " issued by " + issuedBy
            + ": Certificate already exists - ignored.");
        return response;
    }

    private RegisterCertificateResponseType makeInvalidCertificateResult(RegisterCertificateType registerCertificate) {
        RegisterCertificateResponseType response = new RegisterCertificateResponseType();
        response.setResult(ResultTypeUtil.errorResult(ErrorIdType.VALIDATION_ERROR, "Certificate already exists"));
        String certificateId = registerCertificate.getIntyg().getIntygsId().getExtension();
        String issuedBy = registerCertificate.getIntyg().getSkapadAv().getEnhet().getEnhetsId().getExtension();
        LOGGER.error(LogMarkers.VALIDATION, "Failed to create Certificate with id " + certificateId + " issued by " + issuedBy
            + ": Certificate ID already exists for another person.");
        return response;
    }

    private RegisterCertificateResponseType makeInvalidCertificateVersionResult(final RegisterCertificateType registerCertificate) {
        RegisterCertificateResponseType response = new RegisterCertificateResponseType();

        final String version = registerCertificate.getIntyg().getVersion();
        final String typ = registerCertificate.getIntyg().getTyp().getCode();
        final String message = MessageFormat.format("Certificate with type: {0} does not support version: {1}", typ, version);

        response.setResult(ResultTypeUtil.errorResult(ErrorIdType.VALIDATION_ERROR, message));

        final String certificateId = registerCertificate.getIntyg().getIntygsId().getExtension();
        final String issuedBy = registerCertificate.getIntyg().getSkapadAv().getEnhet().getEnhetsId().getExtension();
        final String logMessage = MessageFormat.format("Failed to create Certificate with id {0} issued by {1} : "
            + "Certificate type {2} does not support version: {3}", certificateId, issuedBy, typ, version);
        LOGGER.error(LogMarkers.VALIDATION, logMessage);
        return response;
    }

    private Optional<RegisterCertificateResponseType> validatePersonInPU(String socialSecurityNumber) {
        final Optional<Personnummer> personnummer = Personnummer.createPersonnummer(socialSecurityNumber);
        if (personnummer.isPresent()) {
            final PersonSvar personSvar = puService.getPerson(personnummer.get());
            if (personSvar.getStatus().equals(Status.NOT_FOUND)) {
                LOGGER.error("Patient not found in PU Service. Returning a Logical error");
                return Optional.of(makeValidationErrorResult("No person exists in PU Service with the social security number"));
            } else if (personSvar.getStatus().equals(Status.ERROR)) {
                LOGGER.error("Error when calling PU Service. Returning a Technical error");
                return Optional.of(makeTechnicalErrorResult("Error calling PU Service to validate social security number"));
            } else {
                LOGGER.info("The patient is found in PU Service");
                return Optional.empty();
            }
        } else {
            LOGGER.error("Patients social security number is not correct. Returning a Logical error");
            return Optional.of(makeValidationErrorResult("Social security number is not of correct format."));
        }
    }

    private RegisterCertificateResponseType makeTechnicalErrorResult(String errorString) {
        RegisterCertificateResponseType response = new RegisterCertificateResponseType();
        response.setResult(ResultTypeUtil.errorResult(ErrorIdType.TECHNICAL_ERROR, errorString));
        return response;
    }

    private String xmlToString(RegisterCertificateType registerCertificate) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        JAXBElement<RegisterCertificateType> requestElement = objectFactory.createRegisterCertificate(registerCertificate);
        jaxbContext.createMarshaller().marshal(requestElement, stringWriter);
        return stringWriter.toString();
    }

    private String getIntygsTyp(RegisterCertificateType certificateType) {
        return moduleRegistry.getModuleIdFromExternalId(certificateType.getIntyg().getTyp().getCode());
    }

    private CertificateHolder toCertificateHolder(Intyg intyg, String type, String originalCertificate, String additionalInfo,
        AdditionalMetaData additionalMetaData) {

        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId(intyg.getIntygsId().getExtension());
        certificateHolder.setCareUnitId(intyg.getSkapadAv().getEnhet().getEnhetsId().getExtension());
        certificateHolder.setCareUnitName(intyg.getSkapadAv().getEnhet().getEnhetsnamn());
        certificateHolder.setCareGiverId(intyg.getSkapadAv().getEnhet().getVardgivare().getVardgivareId().getExtension());
        certificateHolder.setSigningDoctorId(intyg.getSkapadAv().getPersonalId().getExtension());
        certificateHolder.setSigningDoctorName(intyg.getSkapadAv().getFullstandigtNamn());
        certificateHolder.setCivicRegistrationNumber(createPnr(intyg));
        certificateHolder.setSignedDate(intyg.getSigneringstidpunkt());
        certificateHolder.setType(type);
        certificateHolder.setTypeVersion(intyg.getVersion());
        certificateHolder.setOriginalCertificate(originalCertificate);
        certificateHolder.setAdditionalInfo(additionalInfo);
        certificateHolder.setCertificateRelation(convertRelation(intyg.getIntygsId().getExtension(), intyg.getRelation()));
        certificateHolder.setAdditionalMetaData(additionalMetaData);
        return certificateHolder;
    }

    private CertificateRelation convertRelation(String intygsId, List<Relation> relations) {
        if (relations != null && relations.size() > 0) {
            return new CertificateRelation(intygsId, relations.get(0).getIntygsId().getExtension(),
                RelationKod.fromValue(relations.get(0).getTyp().getCode()), LocalDateTime.now());
        }
        return null;
    }

    private Personnummer createPnr(Intyg intyg) {
        String personId = null;
        try {
            personId = intyg.getPatient().getPersonId().getExtension();
        } catch (NullPointerException npe) {
            throw new RuntimeException("Could not get patient's personnummer from intyg");
        }

        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));

    }
}
