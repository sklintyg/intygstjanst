package se.inera.certificate.migration.testutils.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import se.inera.certificate.migration.model.Certificate;

public class CertRowMapper implements RowMapper<Certificate> {

    @Override
    public Certificate mapRow(ResultSet rs, int rowNbr) throws SQLException {
        
        Certificate cert = new Certificate();
        cert.setCertificateId(rs.getString("id"));
        
        return cert;
    }

}
