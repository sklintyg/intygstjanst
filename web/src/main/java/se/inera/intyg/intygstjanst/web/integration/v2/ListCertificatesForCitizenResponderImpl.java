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

package se.inera.intyg.intygstjanst.web.integration.v2;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.modules.fkparent.model.converter.CertificateStateHolderConverter;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.v2.ResultTypeUtil;
import se.inera.intyg.common.support.integration.module.exception.MissingConsentException;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.integration.converter.ConverterUtil;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v2.Intyg;

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
    public ListCertificatesForCitizenResponseType listCertificatesForCitizen(String logicalAddress, ListCertificatesForCitizenType parameters) {
        LOGGER.debug("List certificates for citizen. arkiverade={}", parameters.isArkiverade());
        ListCertificatesForCitizenResponseType response = new ListCertificatesForCitizenResponseType();
        response.setIntygsLista(new ListaType());
        response.getIntygsLista().getIntyg(); // initialize list for schema validation, if no certificates

        try {
            final Personnummer personnummer = new Personnummer(parameters.getPersonId().getExtension());
            List<Certificate> certificates = certificateService.listCertificatesForCitizen(
                    personnummer, toStringList(parameters.getIntygTyp()), parameters.getFromDatum(), parameters.getTomDatum());
            for (Certificate certificate : certificates) {
                if (certificateMatchesArkiveradeParameter(parameters.isArkiverade(), certificate.getDeleted())) {
                    response.getIntygsLista().getIntyg().add(convert(certificate));
                }
            }
            response.setResult(ResultTypeUtil.okResult());
            monitoringLogService.logCertificateListedByCitizen(personnummer);
        } catch (ModuleNotFoundException | ModuleException e) {
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "Module error when processing certificates"));
            LOGGER.error(e.getMessage());
        } catch (MissingConsentException ex) {
            response.setResult(ResultTypeUtil.infoResult("NOCONSENT"));
        }

        return response;
    }

    private boolean certificateMatchesArkiveradeParameter(boolean arkiverade, Boolean isDeleted) {
        return (arkiverade && Boolean.TRUE.equals(isDeleted)) || (!arkiverade && !Boolean.TRUE.equals(isDeleted));
    }

    private List<String> toStringList(List<TypAvIntyg> intygTyp) {
        List<String> list = new ArrayList<>();
        if (intygTyp != null) {
            for (TypAvIntyg typAvIntyg : intygTyp) {
                list.add(typAvIntyg.getCode());
            }
        }
        return list;
    }

    private Intyg convert(Certificate certificate) throws ModuleNotFoundException, ModuleException {
        CertificateHolder certificateHolder = ConverterUtil.toCertificateHolder(certificate);

        ModuleApi moduleApi = moduleRegistry.getModuleApi(certificateHolder.getType());
        Intyg intyg = moduleApi.getIntygFromCertificateHolder(certificateHolder);
        intyg.getStatus().addAll(CertificateStateHolderConverter.toIntygsStatusType(certificateHolder.getCertificateStates()));
        return intyg;
    }

}
