package se.inera.certificate.migration.processors;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.util.Assert;

import se.inera.certificate.migration.model.Certificate;

/**
 * Processor which fetches the JSON payload of a Certificate and sets it on the Certificate object.
 *  
 * @author nikpet
 *
 */
public class FetchCertificatePayloadProcessor extends JdbcDaoSupport implements ItemProcessor<Certificate, Certificate> {

    private static Logger log = LoggerFactory.getLogger(FetchCertificatePayloadProcessor.class);
    
    private String sql;

    public FetchCertificatePayloadProcessor() {

    }

    @Override
    protected void initDao() throws Exception {
        super.initDao();
        Assert.notNull(this.sql, "Select SQL must not be null");
    }

    @Override
    public Certificate process(Certificate cert) throws Exception {
        log.debug("Fetching payload for certificate with id {}", cert.getCertificateId());
        
        RowCallbackHandler rowCaller = new CertificateDecoratingCallbackHandler(cert);
        
        getJdbcTemplate().query(sql, rowCaller, cert.getCertificateId());
        
        if (cert.isCertificateJsonEmpty()) {
            log.info("Payload for certificate with id {} is empty, returning null", cert.getCertificateId());
            return null;
        }
        
        return cert;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
    
    private class CertificateDecoratingCallbackHandler implements RowCallbackHandler {

        private Certificate certificate;
        
        public CertificateDecoratingCallbackHandler(Certificate cert) {
            this.certificate = cert;
        } 
        
        @Override
        public void processRow(ResultSet rs) throws SQLException {
            certificate.setCertificateJson(fromBytes(rs.getBytes("DOCUMENT")));
        }
        
        private String fromBytes(byte[] bytes) {
            try {
                return new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Failed to convert bytes to String!", e);
            }
        }
    }
    
}
