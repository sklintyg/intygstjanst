package se.inera.certificate.service.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.integration.test.CertificateResource;
import se.inera.certificate.model.dao.Certificate;

public class IntygBootstrapBean {
    private static final Logger LOG = LoggerFactory.getLogger(IntygBootstrapBean.class);
    
    @Autowired
    private CertificateResource certificateResource;

    @PostConstruct
    public void initData() {

        List<Resource> files = getResourceListing("bootstrap-intyg/*.json");
        for (Resource res : files) {
            LOG.debug("Loading resource " + res.getFilename());
            addIntyg(res);
        }
    }

    private void addIntyg(Resource res) {

        try {
            Certificate certificate = new CustomObjectMapper().readValue(res.getInputStream(), BootstrapCertificate.class);
            certificateResource.insertCertificate(certificate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
