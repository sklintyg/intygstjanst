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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.v2.ResultTypeUtil;
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
import se.inera.intyg.intygstyper.fkparent.model.converter.CertificateStateHolderConverter;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v2.Intyg;

@SchemaValidation
public class ListCertificatesForCareResponderImpl implements ListCertificatesForCareResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCertificatesForCareResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private IntygModuleRegistryImpl moduleRegistry;

    @Override
    public ListCertificatesForCareResponseType listCertificatesForCare(String logicalAddress, ListCertificatesForCareType parameters) {
        ListCertificatesForCareResponseType response = new ListCertificatesForCareResponseType();
        response.setIntygsLista(new ListaType());
        response.getIntygsLista().getIntyg();

        try {
            final Personnummer personnummer = new Personnummer(parameters.getPersonId().getExtension());

            List<Certificate> certificates = certificateService.listCertificatesForCare(personnummer,
                    parameters.getEnhetsId().stream()
                        .map(e -> e.getExtension())
                        .collect(Collectors.toList()));

            for (Certificate certificate : certificates) {
                // If the certificate is deleted by the care giver it is not returned. Note that both revoked and
                // archived certificates are returned
                if (!certificate.isDeletedByCareGiver()) {
                    response.getIntygsLista().getIntyg().add(convert(certificate));
                }
            }

            response.setResult(ResultTypeUtil.okResult());
            monitoringLogService.logCertificateListedByCare(personnummer);

        } catch (ModuleNotFoundException | ModuleException e) {
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "Module error when processing certificates"));
            LOGGER.error(e.getMessage());
        }

        return response;
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