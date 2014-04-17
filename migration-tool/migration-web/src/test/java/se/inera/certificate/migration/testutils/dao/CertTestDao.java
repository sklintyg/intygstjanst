package se.inera.certificate.migration.testutils.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import se.inera.certificate.migration.model.Certificate;
import se.inera.certificate.migration.testutils.CertificateDataInitialiser;

/**
 * DAO for inserting test data into the Intyg database.
 * 
 * @author nikpet
 *
 */
public class CertTestDao extends JdbcDaoSupport {

    private static final String INSERT_ORG_CERT = "INSERT INTO ORIGINAL_CERTIFICATE (RECEIVED, DOCUMENT, CERTIFICATE_ID) " +
            "VALUES (?, ?, ?)";
    
    private static final String INSERT_CERT = "INSERT INTO CERTIFICATE (ID, CERTIFICATE_TYPE, CIVIC_REGISTRATION_NUMBER, " +
    		"CARE_UNIT_NAME, CARE_UNIT_ID, SIGNING_DOCTOR_NAME, SIGNED_DATE, VALID_FROM_DATE, VALID_TO_DATE, DOCUMENT " +
    		") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    
    private static final String SELECT_ORGCERT_ID_NOT_NULL = "SELECT COUNT(CERTIFICATE_ID) FROM ORIGINAL_CERTIFICATE " +
    		"WHERE CERTIFICATE_ID IS NOT NULL";
    
    private static final String DELETE_ALL_CERTS = "DELETE FROM CERTIFICATE WHERE ID IS NOT NULL";
    
    private static final String DELETE_ALL_ORGCERTS = "DELETE FROM ORIGINAL_CERTIFICATE WHERE CERTIFICATE_ID IS NOT NULL";
        
    private static Logger LOG = LoggerFactory.getLogger(CertTestDao.class);
    
    private LobHandler lobHandler = new DefaultLobHandler();
        
    public int countOriginalCertsWithCertificateIDs() {
        return getJdbcTemplate().queryForInt(SELECT_ORGCERT_ID_NOT_NULL);
    }
    
    public void insertCert(Cert cert) {
        LOG.debug("Inserting certificate with id {}", cert.getCertId());        
        getJdbcTemplate().update(INSERT_CERT, new CertPreparedStatementSetter(cert));
    }
    
    public void insertOriginalCertificate(final String certificateId, final byte[] certXML) throws Exception {
                
        if (certXML == null) {
            LOG.error("Can not complete insert operation for certificate with id {}", certificateId);
            return;
        }
        
        LOG.debug("Inserting certificate XML for certificate with id {}", certificateId);
        
        getJdbcTemplate().execute(INSERT_ORG_CERT, new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
            
            @Override
            protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException, DataAccessException {
                Timestamp ts = new Timestamp(System.currentTimeMillis());
                ps.setTimestamp(1, ts);
                lobCreator.setBlobAsBytes(ps, 2, certXML);
                ps.setString(3, certificateId);
            }
        });
        
    }
    
    public void dropAllCertificates() {
        LOG.info("Deleting all Certificates in db");
        getJdbcTemplate().execute(DELETE_ALL_CERTS);
    }
    
    public void dropAllOriginalCertificates() {
        LOG.info("Deleting all OriginalCertificates in db");
        getJdbcTemplate().execute(DELETE_ALL_ORGCERTS);
    }
        
    private class CertPreparedStatementSetter implements PreparedStatementSetter {
        
        private Cert cert;
        
        private DateTimeFormatter dateTimeFormatter;
        
        public CertPreparedStatementSetter(Cert cert) {
            this.cert = cert;
            this.dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd"); 
        }
        
        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
                        
            ps.setString(1, cert.getCertId());
            ps.setString(2, cert.getCertType());
            ps.setString(3, cert.getCivicRegNbr());
            ps.setString(4, cert.getCareUnitName());
            ps.setString(5, cert.getCareUnitId());
            ps.setString(6, cert.getSigningDoctorName());
            
            DateTime certSignedDate = cert.getSignedDate();
            
            ps.setDate(7, new Date(certSignedDate.getMillis()));
            ps.setString(8, dateTimeFormatter.print(certSignedDate));
            ps.setString(9, dateTimeFormatter.print(certSignedDate.plusDays(30)));
            ps.setBytes(10, cert.getDocumentAsBytes());
        }
        
    }
}
