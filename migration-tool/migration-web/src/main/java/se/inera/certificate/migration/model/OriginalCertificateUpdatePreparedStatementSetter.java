package se.inera.certificate.migration.model;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;

public class OriginalCertificateUpdatePreparedStatementSetter  implements ItemPreparedStatementSetter<Certificate> {

    private static Logger log = LoggerFactory.getLogger(OriginalCertificateUpdatePreparedStatementSetter.class);
    
    public OriginalCertificateUpdatePreparedStatementSetter() {
        
    }

    @Override
    public void setValues(Certificate cert, PreparedStatement ps) throws SQLException {
        
        log.debug("Preparing update statement for OriginalCertificate: {} ", cert.toString());
        
        ps.setString(1, cert.getCertificateId());
        ps.setInt(2, cert.getOriginalCertificateId());
                
    }

}
