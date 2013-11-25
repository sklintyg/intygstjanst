package se.inera.certificate.migration.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import se.inera.certificate.migration.model.OriginalCertificate;

public class CheckCertificateExistsProcessor extends JdbcDaoSupport implements
        ItemProcessor<OriginalCertificate, OriginalCertificate> {

    private static final String SELECT_COUNT = "SELECT count(*) FROM certificate WHERE id = ?";

    private static Logger log = LoggerFactory.getLogger(CheckCertificateExistsProcessor.class);
    
    public CheckCertificateExistsProcessor() {
        super();
    }

    @Override
    public OriginalCertificate process(OriginalCertificate orgCert) throws Exception {
        
        if (orgCert == null) {
            return null;
        }
        
        String orgCertId = orgCert.getCertificateId();
        
        if (checkIfCertificateExists(orgCertId)) {
            log.debug("Found matching Certificate for OriginalCertificate {}", orgCertId);
            return orgCert;
        }

        log.warn("OriginalCertificate with id {} has no matching Certificate", orgCertId);
        
        return null;
    }

    public boolean checkIfCertificateExists(String certId) {

        int res = getJdbcTemplate().queryForInt(SELECT_COUNT, certId);

        return (res == 1);
    }

}
