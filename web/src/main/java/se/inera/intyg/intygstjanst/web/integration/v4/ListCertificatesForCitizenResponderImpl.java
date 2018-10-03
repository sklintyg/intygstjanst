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
package se.inera.intyg.intygstjanst.web.integration.v4;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
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
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v4.ListCertificatesForCitizenResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v4.ListCertificatesForCitizenResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v4.ListCertificatesForCitizenType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v4.ListaType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SchemaValidation
public class ListCertificatesForCitizenResponderImpl implements ListCertificatesForCitizenResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCertificatesForCitizenResponderImpl.class);

    private static final List<String> EXCLUDED_CERTIFICATES = Arrays.asList(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private IntygModuleRegistryImpl moduleRegistry;

    @Override
    @PrometheusTimeMethod
    public ListCertificatesForCitizenResponseType listCertificatesForCitizen(String logicalAddress,
            ListCertificatesForCitizenType parameters) {
        LOGGER.debug("List certificates for citizen. arkiverade={}", parameters.isArkiverade());
        ListCertificatesForCitizenResponseType response = new ListCertificatesForCitizenResponseType();
        response.setIntygsLista(new ListaType());
        response.getIntygsLista().getIntyg(); // initialize list for schema validation, if no certificates

        Optional<Personnummer> personnummer =
                Personnummer.createPersonnummer(parameters.getPersonId().getExtension());

        List<Certificate> certificates = certificateService.listCertificatesForCitizen(
                personnummer.orElse(null),
                toStringList(parameters.getIntygTyp()),
                parameters.getFromDatum(),
                parameters.getTomDatum());

        response.getIntygsLista().getIntyg().addAll(certificates.stream()
                .filter(c -> !EXCLUDED_CERTIFICATES.contains(c.getType()))
                .filter(c -> c.isDeleted() == parameters.isArkiverade())
                .map(c -> convert(c, parameters.getPart().getCode()))
                .collect(Collectors.toList()));

        monitoringLogService.logCertificateListedByCitizen(personnummer.orElse(null));

        return response;
    }

    private List<String> toStringList(List<TypAvIntyg> intygTyp) {
        if (intygTyp == null) {
            return new ArrayList<>();
        }
        return intygTyp.stream().map(TypAvIntyg::getCode).collect(Collectors.toList());
    }

    private Intyg convert(Certificate certificate, String part) {
        try {
            CertificateHolder certificateHolder = ConverterUtil.toCertificateHolder(certificate);
            ModuleEntryPoint moduleEntryPoint = moduleRegistry.getModuleEntryPoint(certificateHolder.getType());
            ModuleApi moduleApi = moduleEntryPoint.getModuleApi();
            // Unified handling of all certificate types, maintaining a simple module api
            Intyg intyg = moduleApi.getIntygFromUtlatande(moduleApi.getUtlatandeFromXml(certificateHolder.getOriginalCertificate()));
            intyg.getStatus().addAll(CertificateStateHolderConverter.toIntygsStatusType(certificateHolder.getCertificateStates().stream()
                    .filter(ch -> CertificateStateFilterUtil.filter(ch, part))
                    .collect(Collectors.toList())));
            return intyg;

        } catch (ModuleNotFoundException | ModuleException e) {
            LOGGER.error(e.getMessage());
        }

        return null;
    }

}
