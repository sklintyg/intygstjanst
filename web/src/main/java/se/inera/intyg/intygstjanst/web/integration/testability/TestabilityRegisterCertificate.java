/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration.testability;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import java.io.StringWriter;
import org.springframework.stereotype.Component;
import org.w3._2000._09.xmldsig_.SignatureType;
import org.w3._2002._06.xmldsig_filter2.XPathType;
import se.inera.intyg.common.support.integration.module.exception.CertificateAlreadyExistsException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.ModuleContainerApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.web.integration.util.CertificateHolderConverter;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.ObjectFactory;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.DatePeriodType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PQType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateType;

@Component
public class TestabilityRegisterCertificate {

    private ObjectFactory objectFactory;
    private JAXBContext jaxbContext;

    private final ModuleContainerApi moduleContainer;

    private final IntygModuleRegistry moduleRegistry;
    private final CertificateHolderConverter certificateHolderConverter;

    public TestabilityRegisterCertificate(ModuleContainerApi moduleContainer, IntygModuleRegistry moduleRegistry,
        CertificateHolderConverter certificateHolderConverter) {
        this.moduleContainer = moduleContainer;
        this.moduleRegistry = moduleRegistry;
        this.certificateHolderConverter = certificateHolderConverter;
    }

    @PostConstruct
    public void initializeJaxbContext() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(RegisterCertificateType.class, DatePeriodType.class, SignatureType.class,
            XPathType.class, PartialDateType.class, PQType.class);
        objectFactory = new ObjectFactory();
    }

    public void registerCertificate(RegisterCertificateType registerCertificate) {
        try {
            final String version = registerCertificate.getIntyg().getVersion();
            final String intygsTyp = getIntygsTyp(registerCertificate);
            ModuleApi api = moduleRegistry.getModuleApi(intygsTyp, version);

            final var xml = xmlToString(registerCertificate);
            final var additionalInfo = api.getAdditionalInfo(registerCertificate.getIntyg());
            final var additionalMetaData = api.getAdditionalMetaData(registerCertificate.getIntyg()).orElse(null);
            final var certificateHolder = certificateHolderConverter.convert(registerCertificate.getIntyg(), intygsTyp, xml,
                additionalInfo,
                additionalMetaData);
            moduleContainer.certificateReceived(certificateHolder);
        } catch (ModuleNotFoundException | CertificateAlreadyExistsException | JAXBException | ModuleException
                 | InvalidCertificateException e) {
            throw new RuntimeException(e);
        }
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
}
