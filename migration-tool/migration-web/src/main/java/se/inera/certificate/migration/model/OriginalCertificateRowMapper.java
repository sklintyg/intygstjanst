package se.inera.certificate.migration.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class OriginalCertificateRowMapper implements RowMapper<OriginalCertificate> {

    public OriginalCertificate mapRow(ResultSet rs, int rowNbr) throws SQLException {

        OriginalCertificate certificate = new OriginalCertificate();
        
        certificate.setOriginalCertificateId(rs.getInt("id"));
        certificate.setOriginalCertificate(rs.getBytes("document"));
        
        return certificate ;
    }

}
