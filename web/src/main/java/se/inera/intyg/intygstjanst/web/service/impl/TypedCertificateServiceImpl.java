/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.ag114.support.Ag114EntryPoint;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.infra.certificate.dto.DiagnosedCertificate;
import se.inera.intyg.infra.certificate.dto.SickLeaveCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.web.service.TypedCertificateService;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToDiagnosedCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSickLeaveCertificateConverter;
import se.inera.intyg.schemas.contract.Personnummer;

@Service
public class TypedCertificateServiceImpl implements TypedCertificateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypedCertificateServiceImpl.class);

    private final CertificateDao certificateDao;

    private final IntygModuleRegistry moduleRegistry;

    private final CertificateToDiagnosedCertificateConverter certificateToDiagnosedCertificateConverter;
    private final CertificateToSickLeaveCertificateConverter certificateToSickLeaveCertificateConverter;

    private boolean useNewQuery;

    @Autowired
    public TypedCertificateServiceImpl(CertificateDao certificateDao, IntygModuleRegistry moduleRegistry,
        CertificateToDiagnosedCertificateConverter certificateToDiagnosedCertificateConverter,
        CertificateToSickLeaveCertificateConverter certificateToSickLeaveCertificateConverter,
        @Value("#{new Boolean('${use.certificate.metadata.query:false}')}") boolean useNewQuery) {
        this.certificateDao = certificateDao;
        this.moduleRegistry = moduleRegistry;
        this.certificateToDiagnosedCertificateConverter = certificateToDiagnosedCertificateConverter;
        this.certificateToSickLeaveCertificateConverter = certificateToSickLeaveCertificateConverter;
        this.useNewQuery = useNewQuery;
    }

    @Override
    public List<DiagnosedCertificate> listDiagnosedCertificatesForCareUnits(List<String> units, List<String> certificateTypeList,
        LocalDate fromDate, LocalDate toDate, List<String> doctorIds) {

        LOGGER.debug("Getting diagnosed certificates of types ("
            + String.join(", ", certificateTypeList) + ") for units ("
            + String.join(", ", units) + ")");

        if (useNewQuery) {
            return getDiagnosedCertificatesUsingMetaDataTable(units, certificateTypeList, fromDate, toDate, doctorIds);
        }

        final var certificates = certificateDao.findCertificate(units, certificateTypeList, fromDate, toDate);
        return transformListToDiagnosedCertificates(certificates);
    }

    private List<DiagnosedCertificate> getDiagnosedCertificatesUsingMetaDataTable(List<String> units, List<String> certificateTypeList,
        LocalDate fromDate, LocalDate toDate, List<String> doctorIds) {
        final var certificates = certificateDao
            .findCertificatesUsingMetaDataTable(units, certificateTypeList, fromDate, toDate, doctorIds);
        return transformListWithMetaDataToDiagnosedCertificates(certificates);
    }

    @Override
    public List<String> listDoctorsForCareUnits(List<String> units, List<String> certificateTypeList, LocalDate fromDate,
        LocalDate toDate) {

        LOGGER.debug("Getting signing doctors for certificates of types ("
            + String.join(", ", certificateTypeList) + ") for units ("
            + String.join(", ", units) + ")");

        if (useNewQuery) {
            return certificateDao.findDoctorIds(units, certificateTypeList);
        }

        final var certificates = certificateDao.findCertificate(units, certificateTypeList, fromDate, toDate);
        return certificates.stream().filter(c -> !c.isRevoked())
            .map(Certificate::getSigningDoctorName)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public List<DiagnosedCertificate> listDiagnosedCertificatesForPerson(Personnummer personId, List<String> certificateTypeList,
        LocalDate fromDate, LocalDate toDate, List<String> units) {

        var certificates = certificateDao.findCertificate(personId, certificateTypeList, fromDate, toDate, units);

        LOGGER.debug("Getting diagnosed certificates of types ("
            + String.join(", ", certificateTypeList) + ") for person on units ("
            + String.join(", ", units) + ")");
        return transformListToDiagnosedCertificates(certificates);
    }

    @Override
    public List<SickLeaveCertificate> listSickLeaveCertificatesForPerson(Personnummer personId, List<String> certificateTypeList,
        LocalDate fromDate, LocalDate toDate, List<String> units) {

        var certificates = certificateDao.findCertificate(personId, certificateTypeList, fromDate, toDate, units);

        LOGGER.debug("Getting sickleave certificates of types ("
            + String.join(", ", certificateTypeList) + ") for person on units ("
            + String.join(", ", units) + ")");
        return transformListToSickLeaveCertificates(certificates);
    }

    private List<DiagnosedCertificate> transformListWithMetaDataToDiagnosedCertificates(List<Certificate> certificates) {
        return certificates.stream()
            .map(this::convertToDiagnosedCertificateFromCertificateWithMetaData)
            .collect(Collectors.toList());
    }

    private DiagnosedCertificate convertToDiagnosedCertificateFromCertificateWithMetaData(Certificate certificate) {
        final List<String> diagnosisList = Arrays.asList(
            certificate.getCertificateMetaData()
                .getDiagnoses()
                .replaceAll("\\[|\\]", "")
                .split("\\s*,\\s*")
        );
        return certificateToDiagnosedCertificateConverter.convert(certificate, diagnosisList);
    }

    private static List<String> buildSecondaryDiagnoseCodes(List<String> diagnoseList) {
        if (diagnoseList == null || diagnoseList.size() <= 1) {
            return null;
        }

        return diagnoseList.stream().skip(1).collect(Collectors.toList());
    }

    private List<DiagnosedCertificate> transformListToDiagnosedCertificates(List<Certificate> certificates) {

        return certificates.stream().filter(cert -> !cert.isRevoked()).map(this::convertToDiagnosedCertificate)
            .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private DiagnosedCertificate convertToDiagnosedCertificate(Certificate certificate) {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
            Utlatande utlatande = moduleApi.getUtlatandeFromXml(certificate.getOriginalCertificate().getDocument());

            DiagnosedCertificate diagnosedCertificate;

            switch (certificate.getType()) {
                case LuseEntryPoint.MODULE_ID:
                    diagnosedCertificate = certificateToDiagnosedCertificateConverter.convertLuse(certificate, utlatande);
                    break;
                case LuaefsEntryPoint.MODULE_ID:
                    diagnosedCertificate = certificateToDiagnosedCertificateConverter.convertLuaefs(certificate, utlatande);
                    break;
                case LuaenaEntryPoint.MODULE_ID:
                    diagnosedCertificate = certificateToDiagnosedCertificateConverter.convertLuaena(certificate, utlatande);
                    break;
                default:
                    diagnosedCertificate = null;
                    LOGGER.info("Certificate of type " + certificate.getType() + " could not be converted to DiagnosedCertificate!");
                    break;
            }
            return diagnosedCertificate;
        } catch (Exception e) {
            LOGGER.error("Error converting certificate to DiagnosedCertificate!", e);
            return null;
        }
    }

    private List<SickLeaveCertificate> transformListToSickLeaveCertificates(List<Certificate> certificates) {
        return certificates.stream().filter(cert -> !cert.isRevoked()).map(this::convertToSickLeaveCertificate)
            .collect(Collectors.toList());
    }

    private SickLeaveCertificate convertToSickLeaveCertificate(Certificate certificate) {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
            Utlatande utlatande = moduleApi.getUtlatandeFromXml(certificate.getOriginalCertificate().getDocument());

            SickLeaveCertificate sickLeaveCertificate;

            switch (certificate.getType()) {
                case Ag7804EntryPoint.MODULE_ID:
                    sickLeaveCertificate = certificateToSickLeaveCertificateConverter.convertAg7804(certificate, utlatande);
                    break;
                case Ag114EntryPoint.MODULE_ID:
                    sickLeaveCertificate = certificateToSickLeaveCertificateConverter.convertAg114(certificate, utlatande);
                    break;
                default:
                    sickLeaveCertificate = null;
                    LOGGER.info("Certificate of type " + certificate.getType() + " could not be converted to SickLeaveCertificate!");
                    break;
            }
            return sickLeaveCertificate;
        } catch (Exception e) {
            LOGGER.error("Error converting certificate to SickLeaveCertificate!", e);
            return null;
        }
    }
}
