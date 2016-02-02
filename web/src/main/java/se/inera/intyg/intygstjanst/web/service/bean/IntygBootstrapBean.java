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

package se.inera.intyg.intygstjanst.web.service.bean;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.Partial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import se.inera.intyg.common.support.model.InternalLocalDateInterval;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.converter.util.PartialConverter;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;

public class IntygBootstrapBean {

    private static final Logger LOG = LoggerFactory.getLogger(IntygBootstrapBean.class);

    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    public void setTxManager(PlatformTransactionManager txManager) {
        this.transactionTemplate = new TransactionTemplate(txManager);
    }

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @PostConstruct
    public void initData() throws IOException {

        List<Resource> metadataFiles = getResourceListing("bootstrap-intyg/*-metadata.json");
        List<Resource> contentFiles = getResourceListing("bootstrap-intyg/*-content.json");
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

    private class ResourceFilenameComparator implements Comparator<Resource> {
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
                    certificate.setDocument(IOUtils.toString(content.getInputStream(), "UTF-8"));
                    ModuleApi moduleApi = null;
                    OriginalCertificate originalCertificate = new OriginalCertificate();
                    try {
                        moduleApi = moduleRegistry.getModuleApi(certificate.getType());
                    } catch (ModuleNotFoundException e) {
                        LOG.error("Module {} not found ", certificate.getType());
                    }
                    if (moduleApi != null && moduleApi.marshall(certificate.getDocument()) != null) {
                        originalCertificate.setReceived(new LocalDateTime());
                        originalCertificate.setDocument(moduleApi.marshall(certificate.getDocument()));
                        certificate.setOriginalCertificate(originalCertificate);
                        originalCertificate.setCertificate(certificate);
                    } else {
                        LOG.debug("Got null while populating with original_certificate");
                        originalCertificate.setDocument(certificate.getDocument());
                    }
                    entityManager.persist(originalCertificate);
                    entityManager.persist(certificate);
                } catch (Throwable t) {
                    status.setRollbackOnly();
                    LOG.error("Loading failed of {}: {}", metadata.getFilename(), t.getMessage());
                }
            }
        });

    }

    private List<Resource> getResourceListing(String classpathResourcePath) throws IOException {
        PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
        return Arrays.asList(r.getResources(classpathResourcePath));
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
                        Certificate certificate = new CustomObjectMapper().readValue(metadata.getInputStream(), Certificate.class);
                        certificate.setDocument(IOUtils.toString(content.getInputStream(), "UTF-8"));

                        OriginalCertificate originalCertificate = new OriginalCertificate();

                        ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType());
                        Utlatande utlatande = moduleApi.getUtlatandeFromJson(certificate.getDocument());

                        // For now, we handle fk7263 explicitly. This needs to be improved when we start handling new sjukpenning.
                        if (utlatande instanceof se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande) {
                            SjukfallCertificate sjukfallCertificate = new SjukfallCertificate();
                            sjukfallCertificate.setId(certificate.getId());
                            sjukfallCertificate.setCareGiverId(certificate.getCareGiverId());
                            sjukfallCertificate.setCareUnitId(certificate.getCareUnitId());
                            sjukfallCertificate.setCareUnitName(certificate.getCareUnitName());
                            sjukfallCertificate.setCivicRegistrationNumber(certificate.getCivicRegistrationNumber().getPersonnummer());

                            sjukfallCertificate.setDeleted(false);

                            se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande fkUtlatande = (se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande) utlatande;
                            sjukfallCertificate.setPatientFirstName(fkUtlatande.getGrundData().getPatient().getFornamn());
                            sjukfallCertificate.setPatientLastName(fkUtlatande.getGrundData().getPatient().getEfternamn());
                            sjukfallCertificate.setDiagnoseCode(fkUtlatande.getDiagnosKod());
                            sjukfallCertificate.setSigningDoctorId(fkUtlatande.getGrundData().getSkapadAv().getPersonId());
                            sjukfallCertificate.setSigningDoctorName(fkUtlatande.getGrundData().getSkapadAv().getFullstandigtNamn());
                            sjukfallCertificate.setType(certificate.getType());

                            if (fkUtlatande.getNedsattMed100() != null) {
                                sjukfallCertificate.getSjukfallCertificateWorkCapacity().add(buildWorkCapacity(100, fkUtlatande.getNedsattMed100()));
                                sjukfallCertificate.getSjukfallCertificateWorkCapacity().add(buildWorkCapacity(75, fkUtlatande.getNedsattMed75()));
                                sjukfallCertificate.getSjukfallCertificateWorkCapacity().add(buildWorkCapacity(50, fkUtlatande.getNedsattMed50()));
                                sjukfallCertificate.getSjukfallCertificateWorkCapacity().add(buildWorkCapacity(25, fkUtlatande.getNedsattMed25()));
                            }
                            entityManager.persist(sjukfallCertificate);
                        }


                    } catch (Throwable t) {
                        status.setRollbackOnly();
                        LOG.error("Loading of Sjukfall intyg failed for {}: {}", metadata.getFilename(), t.getMessage());
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SjukfallCertificateWorkCapacity buildWorkCapacity(Integer workCapacity, InternalLocalDateInterval interval) {
        SjukfallCertificateWorkCapacity wc = new SjukfallCertificateWorkCapacity();
        wc.setCapacityPercentage(workCapacity);
        wc.setFromDate(PartialConverter.partialToString(new Partial(interval.fromAsLocalDate())));
        wc.setToDate(PartialConverter.partialToString(new Partial(interval.tomAsLocalDate())));
        return wc;
    }

    // TODO of course, do this properly...
    private boolean isSjukfallsGrundandeIntyg(String type) {
        return type.equalsIgnoreCase("fk7263") || type.equalsIgnoreCase("fk");
    }

}
