/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.fkparent.model.converter.CertificateStateHolderConverter;
import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.common.support.integration.module.exception.MissingConsentException;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.integration.converter.ConverterUtil;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v3.ListCertificatesForCitizenResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v3.ListCertificatesForCitizenResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v3.ListCertificatesForCitizenType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v3.ListaType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@SchemaValidation
public class ListCertificatesForCitizenResponderImpl implements ListCertificatesForCitizenResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCertificatesForCitizenResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private IntygModuleRegistryImpl moduleRegistry;

    @Override
    public ListCertificatesForCitizenResponseType listCertificatesForCitizen(String logicalAddress,
            ListCertificatesForCitizenType parameters) {
        LOGGER.debug("List certificates for citizen. arkiverade={}", parameters.isArkiverade());
        ListCertificatesForCitizenResponseType response = new ListCertificatesForCitizenResponseType();
        response.setIntygsLista(new ListaType());
        response.getIntygsLista().getIntyg(); // initialize list for schema validation, if no certificates

        try {
            final Personnummer personnummer = new Personnummer(parameters.getPersonId().getExtension());
            List<Certificate> certificates = certificateService.listCertificatesForCitizen(
                    personnummer, toStringList(parameters.getIntygTyp()), parameters.getFromDatum(), parameters.getTomDatum());
            for (Certificate certificate : certificates) {
                if (certificateMatchesArkiveradeParameter(parameters.isArkiverade(), certificate)) {
                    response.getIntygsLista().getIntyg().add(convert(certificate));
                }
            }
            response.setResult(ResultTypeUtil.okResult());
            monitoringLogService.logCertificateListedByCitizen(personnummer);
        } catch (ModuleNotFoundException | ModuleException e) {
            LOGGER.error(e.getMessage());
        } catch (MissingConsentException ex) {
            response.setResult(ResultTypeUtil.infoResult("NOCONSENT"));
        }

        return response;
    }

    private boolean certificateMatchesArkiveradeParameter(boolean arkiverade, Certificate certificate) {
        return arkiverade == certificate.isDeleted();
    }

    private List<String> toStringList(List<TypAvIntyg> intygTyp) {
        if (intygTyp == null) {
            return new ArrayList<>();
        }
        return intygTyp.stream().map(TypAvIntyg::getCode).collect(Collectors.toList());
    }

    private Intyg convert(Certificate certificate) throws ModuleNotFoundException, ModuleException {
        CertificateHolder certificateHolder = ConverterUtil.toCertificateHolder(certificate);
        ModuleApi moduleApi = moduleRegistry.getModuleApi(certificateHolder.getType());
        // Unified handling of all certificate types, maintaining a simple module api
        Intyg intyg = moduleApi.getIntygFromUtlatande(moduleApi.getUtlatandeFromXml(certificateHolder.getOriginalCertificate()));
        intyg.getStatus().addAll(CertificateStateHolderConverter.toIntygsStatusType(certificateHolder.getCertificateStates()));
        return intyg;
    }

}
