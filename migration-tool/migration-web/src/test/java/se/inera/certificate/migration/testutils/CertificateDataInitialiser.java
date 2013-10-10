package se.inera.certificate.migration.testutils;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

public class CertificateDataInitialiser extends JdbcDaoSupport  {

    private static final String INSERT_ORG_CERT = "INSERT INTO original_certificate (RECEIVED, DOCUMENT) " +
    		"VALUES (?, ?)";
    
    private static Logger LOG = LoggerFactory.getLogger(CertificateDataInitialiser.class);
    
    private LobHandler lobHandler = new DefaultLobHandler();
    
    @PostConstruct
    public void load() throws Exception {
        LOG.info("Loading data...");
        
        final byte[] certXML = readOriginalCertificateFromFile("data/maximalt-fk7263.xml");
        
        getJdbcTemplate().execute(INSERT_ORG_CERT, new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
            
            @Override
            protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException, DataAccessException {
                Timestamp ts = new Timestamp(System.currentTimeMillis());
                ps.setTimestamp(1, ts);
                lobCreator.setBlobAsBytes(ps, 2, certXML);
            }
        });
        
    }
    
    private byte[] readOriginalCertificateFromFile(String filePath) throws Exception {
        
        Resource fileRes = new ClassPathResource(filePath);
        
        return FileUtils.readFileToByteArray(fileRes.getFile()); 
    }
}
