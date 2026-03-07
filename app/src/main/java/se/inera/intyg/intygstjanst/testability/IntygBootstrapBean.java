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
package se.inera.intyg.intygstjanst.testability;

import com.google.common.io.Resources;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.CertificateMetaData;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.application.sickleave.converter.CertificateToSjukfallCertificateConverter;

@Component
@Profile("bootstrap")
@DependsOn({"transportConverterUtil", "internalConverterUtil", "transportToInternal", "befattningService"})
public class IntygBootstrapBean {

    private static final Logger LOG = LoggerFactory.getLogger(IntygBootstrapBean.class);
    private static final String DEFAULT_TYPE_VERSION_FALLBACK = "1.0";

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private CertificateToSjukfallCertificateConverter certificateToSjukfallCertificateConverter;

    @Autowired
    private IntygBootstrapPersister persister;

    @PostConstruct
    public void initData() {
        bootstrapModuleCertificates();
        bootstrapLocalCertificates();
    }

    private void bootstrapModuleCertificates() {
        for (Resource resource : getResourceListing("classpath*:module-bootstrap-certificate/*.xml")) {
            // Make spotbugs happy (it disallows nullpointerexceptions, but we don't really care about that here).
            if (resource == null) {
                throw new RuntimeException("Nullpointer.");
            }
            String resourceFilename = resource.getFilename();
            if (resourceFilename == null) {
                throw new RuntimeException("Nullpointer.");
            }
            try {
                Objects.requireNonNull(resourceFilename);
                if (resourceFilename.contains("locked")) {
                    continue;
                }

                String moduleName = resourceFilename.split("__", -1)[0];
                String intygMajorTypeVersion = resourceFilename.split("\\.", -1)[1];
                LOG.info("Bootstrapping certificate '{}' from module {} (version {})", resource.getFilename(), moduleName,
                    intygMajorTypeVersion);
                String xmlString = Resources.toString(resource.getURL(), StandardCharsets.UTF_8);

                ModuleApi moduleApi = moduleRegistry.getModuleApi(moduleName, intygMajorTypeVersion);
                bootstrapCertificate(xmlString, moduleApi,
                    moduleRegistry.getModuleEntryPoint(moduleName).getDefaultRecipient());
            } catch (Exception e) {
                LOG.error("Could not bootstrap certificate in file '{}'", resourceFilename, e);
            }
        }
    }

    private void bootstrapCertificate(String xmlString, ModuleApi moduleApi, String defaultRecipient) throws ModuleException {
        final Utlatande utlatande = moduleApi.getUtlatandeFromXml(xmlString);
        final String additonalInfo = moduleApi.getAdditionalInfo(moduleApi.getIntygFromUtlatande(utlatande));

        Certificate certificate = new Certificate(utlatande.getId());
        certificate.setAdditionalInfo(additonalInfo);
        certificate.setCareGiverId(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid());
        certificate.setCareUnitId(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
        certificate.setCareUnitName(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());
        certificate.setCivicRegistrationNumber(utlatande.getGrundData().getPatient().getPersonId());
        certificate.setDeletedByCareGiver(false);
        OriginalCertificate originalCertificate = new OriginalCertificate(utlatande.getGrundData().getSigneringsdatum(),
            xmlString, certificate);
        certificate.setOriginalCertificate(originalCertificate);
        certificate.setSignedDate(utlatande.getGrundData().getSigneringsdatum());
        certificate.setSigningDoctorName(utlatande.getGrundData().getSkapadAv().getFullstandigtNamn());
        certificate.setStates(Arrays.asList(
            new CertificateStateHistoryEntry("HSVARD", CertificateState.RECEIVED,
                utlatande.getGrundData().getSigneringsdatum().plusMinutes(1)),
            new CertificateStateHistoryEntry(defaultRecipient, CertificateState.SENT,
                utlatande.getGrundData().getSigneringsdatum().plusMinutes(2))));
        certificate.setType(utlatande.getTyp());
        certificate.setTypeVersion(utlatande.getTextVersion() != null ? utlatande.getTextVersion() : DEFAULT_TYPE_VERSION_FALLBACK);
        certificate.setValidFromDate(null);
        certificate.setValidToDate(null);
        certificate.setWireTapped(false);

        CertificateMetaData metaData = new CertificateMetaData(certificate, utlatande.getGrundData().getSkapadAv().getPersonId(),
            utlatande.getGrundData().getSkapadAv().getFullstandigtNamn(), false, null);
        certificate.setCertificateMetaData(metaData);

        // Delegate to @Transactional service (proxy-based, so each certificate gets its own transaction)
        persister.persistCertificate(certificate, originalCertificate, metaData, utlatande);
    }

    private void bootstrapLocalCertificates() {
        List<Resource> metadataFiles = getResourceListing("bootstrap-intyg/*-metadata.json");
        List<Resource> contentFiles = getResourceListing("bootstrap-intyg/*-content.xml");
        metadataFiles.sort(new ResourceFilenameComparator());
        contentFiles.sort(new ResourceFilenameComparator());
        int count = metadataFiles.size();
        for (int i = 0; i < count; i++) {
            Resource metadata = metadataFiles.get(i);
            Resource content = contentFiles.get(i);
            LOG.debug("Loading metadata " + metadata.getFilename() + " and content " + content.getFilename());
            addIntyg(metadata, content);
            addSjukfall(metadata, content);
        }
    }

    private static class ResourceFilenameComparator implements Comparator<Resource> {

        @Override
        public int compare(Resource arg0, Resource arg1) {
            String arg0Filename = arg0.getFilename();
            String arg1Filename = arg1.getFilename();
            if (arg0Filename == null || arg1Filename == null) {
                throw new NullPointerException();
            }
            String[] firstObjectsStrings = arg0Filename.split("-", -1);
            String[] secondObjectsStrings = arg1Filename.split("-", -1);
            int first = 0, second = 0;
            final int indexOfInt = 1;
            try {
                first = Integer.parseInt(firstObjectsStrings[indexOfInt]);
            } catch (NumberFormatException e) {
                LOG.error("Could not parse int in filename " + arg0.getFilename(), e);
            }
            try {
                second = Integer.parseInt(secondObjectsStrings[indexOfInt]);
            } catch (NumberFormatException e) {
                LOG.error("Could not parse int in filename " + arg1.getFilename(), e);
            }
            return Integer.compare(first, second);
        }
    }

    private void addIntyg(final Resource metadata, final Resource content) {
        try {
            Certificate certificate = new CustomObjectMapper().readValue(metadata.getInputStream(), Certificate.class);
            String contentString = Resources.toString(content.getURL(), StandardCharsets.UTF_8);
            OriginalCertificate originalCertificate = new OriginalCertificate(
                certificate.getSignedDate(), contentString, certificate);

            ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
            final Utlatande utlatande = moduleApi.getUtlatandeFromXml(contentString);
            certificate.setAdditionalInfo(moduleApi.getAdditionalInfo(moduleApi.getIntygFromUtlatande(utlatande)));

            CertificateMetaData metaData = new CertificateMetaData(certificate,
                utlatande.getGrundData().getSkapadAv().getPersonId(),
                utlatande.getGrundData().getSkapadAv().getFullstandigtNamn(), false, null);

            persister.persistLocalCertificate(certificate, originalCertificate, metaData);
        } catch (Exception e) {
            LOG.error("Loading failed of {}", metadata.getFilename(), e);
        }
    }

    private List<Resource> getResourceListing(String classpathResourcePath) {
        try {
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            return Arrays.asList(r.getResources(classpathResourcePath));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void addSjukfall(final Resource metadata, final Resource content) {
        try {
            Certificate certificate = new CustomObjectMapper().readValue(metadata.getInputStream(), Certificate.class);
            if (!isSjukfallsGrundandeIntyg(certificate.getType())) {
                return;
            }
            String contentString = Resources.toString(content.getURL(), StandardCharsets.UTF_8);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
            Utlatande utlatande = moduleApi.getUtlatandeFromXml(contentString);

            persister.persistSjukfall(certificate, utlatande);
        } catch (Exception e) {
            LOG.error("Loading of Sjukfall intyg failed for {}", metadata.getFilename(), e);
        }
    }

    private boolean isSjukfallsGrundandeIntyg(String type) {
        return Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(type) || LisjpEntryPoint.MODULE_ID.equalsIgnoreCase(type);
    }
}
