package se.inera.certificate.service.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.dao.Certificate;

public class IntygBootstrapBean {

    private static final Logger LOG = LoggerFactory.getLogger(IntygBootstrapBean.class);

    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    public void setTxManager(PlatformTransactionManager txManager) {
        this.transactionTemplate = new TransactionTemplate(txManager);
    }

    @PostConstruct
    public void initData() {

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
        }
    }

    private class ResourceFilenameComparator implements Comparator<Resource> {
        @Override
        public int compare(Resource arg0, Resource arg1) {
            String[] firstObjectsStrings = arg0.getFilename().split("-");
            String[] secondObjectsStrings = arg1.getFilename().split("-");
            int first = 0, second = 0;
            for (String s : firstObjectsStrings) {
                try {
                    first = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                }
            }
            for (String s : secondObjectsStrings) {
                try {
                    second = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                }
            }
            if (first > second) {
                return 1;
            } else if (first < second) {
                return -1;
            }
            return 0;
        }

    }

    private void addIntyg(final Resource metadata, final Resource content) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    Certificate certificate = new CustomObjectMapper().readValue(metadata.getInputStream(), Certificate.class);
                    certificate.setDocument(IOUtils.toString(content.getInputStream(), "UTF-8"));
                    entityManager.persist(certificate);
                } catch (Throwable t) {
                    status.setRollbackOnly();
                    LOG.error("Loading failed: " + t.getMessage());
                }
            }
        });

    }

    private List<Resource> getResourceListing(String classpathResourcePath) {
        try {
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            return Arrays.asList(r.getResources(classpathResourcePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
