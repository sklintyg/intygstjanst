package se.inera.certificate.migration.model;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class CertificateRowMapper implements RowMapper<Certificate> {

    public CertificateRowMapper() {

    }

    @Override
    public Certificate mapRow(ResultSet rs, int rowNbr) throws SQLException {

        Certificate certificate = new Certificate();

        certificate.setCertificateId(rs.getString("id"));
        certificate.setCertificateJson(fromBytes(rs.getBytes("document")));

        return certificate;
    }

    private String fromBytes(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to convert bytes to String!", e);
        }
    }
}
