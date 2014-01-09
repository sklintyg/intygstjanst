package se.inera.certificate.migration.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.util.Assert;

import se.inera.certificate.migration.model.Certificate;

/**
 * Processor that checks if a Certificate has an entry in the CERTIFICATE_STATE table with the state CANCELLED.
 * 
 * @author nikpet
 *
 */
public class CheckCertificateStateProcessor extends JdbcDaoSupport implements ItemProcessor<Certificate, Certificate> {

    private static final String CANCELLED_STATE = "CANCELLED";

    private static Logger log = LoggerFactory.getLogger(CheckCertificateStateProcessor.class);

    private String checkSql = null;

    public CheckCertificateStateProcessor() {

    }

    @Override
    public Certificate process(Certificate certificate) throws Exception {

        log.debug("Checking state of certificate {}", certificate.getCertificateId());

        if (isCertificateRevoked(certificate)) {
            log.debug("Certificate {} has state revoked", certificate.getCertificateId());
            certificate.setRevoked(true);
        }

        return certificate;
    }

    private boolean isCertificateRevoked(Certificate certificate) {
        int res = getJdbcTemplate().queryForInt(checkSql, certificate.getCertificateId(), CANCELLED_STATE);
        return (res > 0);
    }

    @Override
    protected void initDao() throws Exception {
        super.initDao();
        Assert.notNull(checkSql, "Check state SQL must not be null");
    }
    
    public String getCheckSql() {
        return checkSql;
    }

    public void setCheckSql(String checkSql) {
        this.checkSql = checkSql;
    }

}
