package se.inera.certificate.migration.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * RowMapper which extracts data retrieved from the ORIGINAL_CERTIFICATES table
 * and turns it into a Certifcate object.
 * 
 * @author nikpet
 * 
 */
public class CertificateStep1RowMapper implements RowMapper<Certificate> {

    public CertificateStep1RowMapper() {

    }

    @Override
    public Certificate mapRow(ResultSet rs, int rowNbr) throws SQLException {

        Certificate certificate = new Certificate();
        certificate.setCertificateId(rs.getString("CERTIFICATE_ID"));
        certificate.setOriginalCertificateId(rs.getInt("ID"));
        certificate.setCertificateXml(rs.getBytes("DOCUMENT"));
        
        return certificate;
    }
}
