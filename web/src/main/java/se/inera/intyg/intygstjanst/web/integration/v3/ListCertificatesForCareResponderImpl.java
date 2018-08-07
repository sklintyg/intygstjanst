/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.fkparent.model.converter.CertificateStateHolderConverter;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.integration.converter.ConverterUtil;
import se.inera.intyg.intygstjanst.web.integration.util.CertificateStateFilterUtil;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListaType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@SchemaValidation
public class ListCertificatesForCareResponderImpl implements ListCertificatesForCareResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCertificatesForCareResponderImpl.class);

    private static final String HSVARD_PARTKOD = "HSVARD";

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private IntygModuleRegistryImpl moduleRegistry;

    @Override
    @PrometheusTimeMethod
    public ListCertificatesForCareResponseType listCertificatesForCare(String logicalAddress, ListCertificatesForCareType parameters) {
        ListCertificatesForCareResponseType response = new ListCertificatesForCareResponseType();
        response.setIntygsLista(new ListaType());
        response.getIntygsLista().getIntyg();

        final Optional<Personnummer> personnummer =
                Personnummer.createPersonnummer(parameters.getPersonId().getExtension());

        List<Certificate> certificates = certificateService.listCertificatesForCare(personnummer.orElse(null),
                parameters.getEnhetsId().stream().map(HsaId::getExtension).collect(Collectors.toList()));

        for (Certificate certificate : certificates) {
            // If the certificate is deleted by the care giver it is not returned.
            // Note that both revoked and archived certificates are returned
            if (!certificate.isDeletedByCareGiver()) {
                Intyg intyg = convert(certificate);
                if (intyg != null) {
                    // Add all certificates that were successfully converted.
                    // We are trying to provide callee with as much data as possible.
                    response.getIntygsLista().getIntyg().add(intyg);
                }
            }
        }

        monitoringLogService.logCertificateListedByCare(personnummer.orElse(null));
        return response;
    }

    private Intyg convert(Certificate certificate) {
        try {
            CertificateHolder certificateHolder = ConverterUtil.toCertificateHolder(certificate);
            ModuleEntryPoint moduleEntryPoint = moduleRegistry.getModuleEntryPoint(certificateHolder.getType());
            ModuleApi moduleApi = moduleEntryPoint.getModuleApi();
            // Unified handling of all certificate types, maintaining a simple module api
            Intyg intyg = moduleApi.getIntygFromUtlatande(moduleApi.getUtlatandeFromXml(certificateHolder.getOriginalCertificate()));
            intyg.getStatus().addAll(CertificateStateHolderConverter.toIntygsStatusType(certificateHolder.getCertificateStates().stream()
                    .filter(ch -> CertificateStateFilterUtil.filter(ch, HSVARD_PARTKOD, moduleEntryPoint.getDefaultRecipient()))
                    .collect(Collectors.toList())));
            return intyg;

        } catch (ModuleNotFoundException | ModuleException e) {
            LOGGER.error(e.getMessage());
        }

        return null;
    }

}
