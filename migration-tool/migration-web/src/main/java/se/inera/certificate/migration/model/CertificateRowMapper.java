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
public class CertificateRowMapper implements RowMapper<Certificate> {

    public CertificateRowMapper() {

    }

    @Override
    public Certificate mapRow(ResultSet rs, int rowNbr) throws SQLException {

        Certificate certificate = new Certificate();

        certificate.setOriginalCertificateId(rs.getInt("ID"));
        certificate.setCertificateId(rs.getString("CERTIFICATE_ID"));

        return certificate;
    }
}
