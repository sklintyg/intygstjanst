package se.inera.certificate.migration.processors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import se.inera.certificate.migration.model.OriginalCertificate;
import se.inera.certificate.migration.testutils.dbunit.AbstractDbUnitSpringTest;

import com.github.springtestdbunit.annotation.DatabaseSetup;

@DatabaseSetup("/data/certificate-dataset.xml") 
public class CheckCertificateExistsProcessorTest extends AbstractDbUnitSpringTest {

    @Autowired
    @Qualifier("certificateDataSource")
    private DataSource dataSource;
    
    @Value("${certificate.checkexists.sql}")
    private String checkExistsSql;
    
    private CheckCertificateExistsProcessor processor;
    
    public CheckCertificateExistsProcessorTest() {

    }

    @Before
    public void setup() {
        processor = new CheckCertificateExistsProcessor();
        processor.setDataSource(dataSource);
        processor.setCertificateCheckSql(checkExistsSql);
    }
        
    @Test
    public void checkThatCertificateExists() throws Exception {
        
        OriginalCertificate cert = new OriginalCertificate();
        cert.setCertificateId("certificate004");
        
        cert = processor.process(cert);
        
        assertNotNull(cert);
    }
    
    @Test
    public void checkThatCertificateNotExists() throws Exception {
        
        OriginalCertificate cert = new OriginalCertificate();
        cert.setCertificateId("certificate999");
        
        cert = processor.process(cert);
        
        assertNull(cert);
    }
}
