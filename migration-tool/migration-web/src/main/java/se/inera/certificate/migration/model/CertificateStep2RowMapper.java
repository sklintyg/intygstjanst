package se.inera.certificate.migration.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.LocalDateTime;
import org.springframework.jdbc.core.RowMapper;

/**
 * RowMapper which extracts data retrieved from the ORIGINAL_CERTIFICATES table
 * and turns it into a Certifcate object.
 * 
 * @author nikpet
 * 
 */
public class CertificateStep2RowMapper implements RowMapper<Certificate> {

    public CertificateStep2RowMapper() {

    }

    @Override
    public Certificate mapRow(ResultSet rs, int rowNbr) throws SQLException {

        Certificate certificate = new Certificate();
        //certificate.setOriginalCertificateId(rs.getInt("ID"));
        certificate.setCertificateId(rs.getString("CERT_ID"));
        certificate.setCertificateJson(rs.getBytes("CERT_JSON"));

        Date sqlDate = rs.getDate("SIGNED_DATE");        
        LocalDateTime signedDate = new LocalDateTime(sqlDate.getTime());
        certificate.setSignedDate(signedDate);
        
        return certificate;
    }
}
