/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.service.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.infra.certificate.dto.CertificateListEntry;
import se.inera.intyg.infra.certificate.dto.CertificateListRequest;
import se.inera.intyg.infra.certificate.dto.CertificateListResponse;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.web.service.CertificateListService;
import se.inera.intyg.schemas.contract.Personnummer;

@Service
public class CertificateListServiceImpl implements CertificateListService {

    private final CertificateDao certificateDao;
    private final IntygModuleRegistry intygModuleRegistry;

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateListServiceImpl.class);

    @Autowired
    public CertificateListServiceImpl(CertificateDao certificateDao, IntygModuleRegistry intygModuleRegistry) {
        this.certificateDao = certificateDao;
        this.intygModuleRegistry = intygModuleRegistry;
    }

    @Override
    public CertificateListResponse listCertificatesForDoctor(CertificateListRequest parameters) {
        CertificateListResponse certificateListResponse = new CertificateListResponse();

        var civicRegistrationNumber = parameters.getCivicRegistrationNumber() == null
            || parameters.getCivicRegistrationNumber().equals("") ? null
            : Personnummer.createPersonnummer(parameters.getCivicRegistrationNumber()).get();

        var certificates = certificateDao.findCertificates(civicRegistrationNumber, parameters.getUnitIds(), parameters.getFromDate(),
            parameters.getToDate(), parameters.getOrderBy(), parameters.isOrderAscending());
        var sentCertificates = getSentCertificates(certificates);
        LOGGER.debug("Getting signed certificates for units (" + Arrays.toString(parameters.getUnitIds()) + ")");

        var certificateList = certificates.stream()
            .filter(cert -> !cert.isRevoked())
            .map(this::convertToUtlatande)
            .filter(Objects::nonNull)
            .filter(cert -> cert.getGrundData().getSkapadAv().getPersonId().equals(parameters.getHsaId()))
            .map(c -> convertToCertificateListEntry(c, sentCertificates))
            .map(this::addCertificateTypeName)
            .collect(Collectors.toList());

        sortList(certificateList, parameters.getOrderBy(), parameters.isOrderAscending());
        certificateListResponse.setTotalCount(certificateList.size());
        certificateListResponse.setCertificates(getSubList(certificateList, parameters.getStartFrom(), parameters.getPageSize()));
        return certificateListResponse;
    }

    private void sortList(List<CertificateListEntry> certificates, String orderBy, boolean ascending) {
        Comparator<CertificateListEntry> comparator = null;
        if (orderBy != null) {
            switch (orderBy) {
                case "type":
                    comparator = Comparator.comparing(CertificateListEntry::getCertificateTypeName);
                    break;
                case "status":
                    comparator = (c1, c2) -> Boolean.compare(c2.isSent(), c1.isSent());
                    break;
                default:
                    break;
            }
        }
        if (comparator != null) {
            if (!ascending) {
                comparator = comparator.reversed();
            }
            certificates.sort(comparator);
        }
    }

    private Utlatande convertToUtlatande(Certificate certificate) {
        try {
            ModuleApi moduleApi = intygModuleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
            return moduleApi.getUtlatandeFromXml(certificate.getOriginalCertificate().getDocument());
        } catch (Exception e) {
            LOGGER.error("Error converting certificate to utlatande.", e);
            return null;
        }
    }

    private CertificateListEntry convertToCertificateListEntry(Utlatande utlatande, List<Certificate> sentCertificates) {
        CertificateListEntry certificateListEntry = new CertificateListEntry();
        certificateListEntry.setCivicRegistrationNumber(utlatande.getGrundData().getPatient().getPersonId().getPersonnummer());
        certificateListEntry.setSignedDate(utlatande.getGrundData().getSigneringsdatum());
        certificateListEntry.setCertificateType(utlatande.getTyp());
        certificateListEntry.setCertificateId(utlatande.getId());
        certificateListEntry.setCertificateTypeVersion(utlatande.getTextVersion());
        certificateListEntry.setSent(isCertificateSent(sentCertificates, utlatande.getId()));
        return certificateListEntry;
    }

    private boolean isCertificateSent(List<Certificate> sentCertificates, String certificateId) {
        return sentCertificates.stream().anyMatch(c -> c.getId().equals(certificateId));
    }

    private List<Certificate> getSentCertificates(List<Certificate> certificates) {
        return certificates.stream()
            .filter(c -> c.getStates().stream().anyMatch(state -> state.getState() == CertificateState.SENT)).collect(Collectors.toList());
    }

    private CertificateListEntry addCertificateTypeName(CertificateListEntry certificateListEntry) {
        try {
            final ModuleEntryPoint moduleEntryPoint = intygModuleRegistry.getModuleEntryPoint(certificateListEntry.getCertificateType());
            certificateListEntry.setCertificateTypeName(moduleEntryPoint.getModuleName());
        } catch (ModuleNotFoundException e) {
            certificateListEntry.setCertificateTypeName(certificateListEntry.getCertificateType());
            LOGGER.error("Could not find ModuleEntryPoint for certificate type: " + certificateListEntry.getCertificateType());
        }
        return certificateListEntry;
    }

    private List<CertificateListEntry> getSubList(List<CertificateListEntry> certificates, int startFrom, int pageSize) {
        if (pageSize > certificates.size()) {
            return certificates;
        } else {
            int endPoint = Math.min(certificates.size(), startFrom + pageSize);
            return certificates.subList(startFrom, endPoint);
        }
    }
}
