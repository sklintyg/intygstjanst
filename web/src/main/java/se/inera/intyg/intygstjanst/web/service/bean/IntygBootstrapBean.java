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
package se.inera.intyg.intygstjanst.web.service.bean;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSjukfallCertificateConverter;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IntygBootstrapBean {

    private static final Logger LOG = LoggerFactory.getLogger(IntygBootstrapBean.class);

    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private CertificateToSjukfallCertificateConverter certificateToSjukfallCertificateConverter;

    @Autowired
    public void setTxManager(PlatformTransactionManager txManager) {
        this.transactionTemplate = new TransactionTemplate(txManager);
    }

    @PostConstruct
    public void initData() {
        bootstrapModuleCertificates();
        bootstrapLocalCertificates();
    }

    private void bootstrapModuleCertificates() {
        for (Resource resource : getResourceListing("classpath*:module-bootstrap-certificate/*.xml")) {
            try {
                String moduleName = resource.getFilename().split("__")[0];
                LOG.info("Bootstrapping certificate '{}' from module {}", resource.getFilename(), moduleName);
                String xmlString = Resources.toString(resource.getURL(), Charsets.UTF_8);
                bootstrapCertificate(xmlString, moduleRegistry.getModuleApi(moduleName).getUtlatandeFromXml(xmlString),
                        moduleRegistry.getModuleEntryPoint(moduleName).getDefaultRecipient());
            } catch (IOException | ModuleNotFoundException | ModuleException e) {
                LOG.error("Could not bootstrap certificate in file '{}'", resource.getFilename(), e);
            }
        }
    }

    private void bootstrapCertificate(String xmlString, Utlatande utlatande, String defaultRecipient) {
        transactionTemplate.execute((TransactionStatus status) -> {
            Certificate certificate = new Certificate(utlatande.getId());
            if (!entityManager.contains(certificate)) {
                certificate.setAdditionalInfo(null); // Should this be populated?
                certificate.setCareGiverId(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid());
                certificate.setCareUnitId(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
                certificate.setCareUnitName(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());
                certificate.setCivicRegistrationNumber(utlatande.getGrundData().getPatient().getPersonId());
                certificate.setDeletedByCareGiver(false);
                OriginalCertificate originalCertificate;
                originalCertificate = new OriginalCertificate(utlatande.getGrundData().getSigneringsdatum(),
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
                certificate.setValidFromDate(null);
                certificate.setValidToDate(null);
                certificate.setWireTapped(false);

                entityManager.persist(originalCertificate);
                entityManager.persist(certificate);

                // Handle sjukfall creation for applicable intygstyper (fk7273, lisjp)
                if (isSjukfallsGrundandeIntyg(certificate.getType())) {
                    if (certificateToSjukfallCertificateConverter.isConvertableFk7263(utlatande)) {
                        entityManager.persist(certificateToSjukfallCertificateConverter.convertFk7263(certificate, utlatande));
                    }
                    if (certificateToSjukfallCertificateConverter.isConvertableLisjp(utlatande)) {
                        entityManager.persist(certificateToSjukfallCertificateConverter.convertLisjp(certificate, utlatande));
                    }
                }
            }
            return null;
        });
    }

    private void bootstrapLocalCertificates() {
        List<Resource> metadataFiles = getResourceListing("bootstrap-intyg/*-metadata.json");
        List<Resource> contentFiles = getResourceListing("bootstrap-intyg/*-content.xml");
        Collections.sort(metadataFiles, new ResourceFilenameComparator());
        Collections.sort(contentFiles, new ResourceFilenameComparator());
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
            String[] firstObjectsStrings = arg0.getFilename().split("-");
            String[] secondObjectsStrings = arg1.getFilename().split("-");
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
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    Certificate certificate = new CustomObjectMapper().readValue(metadata.getInputStream(), Certificate.class);
                    String contentString = Resources.toString(content.getURL(), Charsets.UTF_8);
                    OriginalCertificate originalCertificate = new OriginalCertificate(certificate.getSignedDate(), contentString,
                            certificate);
                    entityManager.persist(originalCertificate);
                    entityManager.persist(certificate);
                } catch (Exception e) {
                    status.setRollbackOnly();
                    LOG.error("Loading failed of {}: {}", metadata.getFilename(), e);
                }
            }
        });

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

            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        String contentString = Resources.toString(content.getURL(), Charsets.UTF_8);

                        ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType());
                        Utlatande utlatande = moduleApi.getUtlatandeFromXml(contentString);

                        if (certificateToSjukfallCertificateConverter.isConvertableFk7263(utlatande)) {
                            SjukfallCertificate sjukfallCertificate = certificateToSjukfallCertificateConverter.convertFk7263(certificate,
                                    utlatande);
                            entityManager.persist(sjukfallCertificate);
                        }
                        if (certificateToSjukfallCertificateConverter.isConvertableLisjp(utlatande)) {
                            SjukfallCertificate sjukfallCertificate = certificateToSjukfallCertificateConverter.convertLisjp(certificate,
                                    utlatande);
                            entityManager.persist(sjukfallCertificate);
                        }
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        LOG.error("Loading of Sjukfall intyg failed for {}: {}", metadata.getFilename(), e);
                    }
                }
            });

        } catch (IOException e) {
            LOG.error("Loading of Sjukfall intyg failed for {}: {}", metadata.getFilename(), e);
        }
    }

    private boolean isSjukfallsGrundandeIntyg(String type) {
        return Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(type) || LisjpEntryPoint.MODULE_ID.equalsIgnoreCase(type);
    }

}
