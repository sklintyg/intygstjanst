package se.inera.certificate.migration.model;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;

public class CertificateItemPreparedStatementSetter implements ItemPreparedStatementSetter<Certificate> {
    
    private static Logger LOG = LoggerFactory.getLogger(CertificateItemPreparedStatementSetter.class); 
    
    @Override
    public void setValues(Certificate cert, PreparedStatement ps) throws SQLException {
        
        LOG.debug("Preparing update statement for cert {}, with JSON payload of {} chars ", cert.getCertificateId()
                , cert.getCertificateJsonSize());
        
        ps.setBytes(1, cert.getCertificateJsonAsBytes());
        ps.setString(2, cert.getCertificateId());

    }

}
