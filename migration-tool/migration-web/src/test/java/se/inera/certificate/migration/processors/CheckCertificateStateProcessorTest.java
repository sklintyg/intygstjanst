package se.inera.certificate.migration.processors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import se.inera.certificate.migration.model.Certificate;
import se.inera.certificate.migration.testutils.dbunit.AbstractDbUnitSpringTest;

import com.github.springtestdbunit.annotation.DatabaseSetup;

@ContextConfiguration({"/spring/props-context.xml","/spring/data-source-context.xml"})
@DatabaseSetup("/data/certificate-dataset.xml")
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class CheckCertificateStateProcessorTest extends AbstractDbUnitSpringTest {

    @Autowired
    @Qualifier("certificateDataSource")
    private DataSource dataSource;
    
    @Value("${certs2stats.statecheck.sql}")
    private String stateCheckSql;
    
    private CheckCertificateStateProcessor processor;
    
    public CheckCertificateStateProcessorTest() {

    }

    @Before
    public void setup() {
        processor = new CheckCertificateStateProcessor();
        processor.setDataSource(dataSource);
        processor.setCheckSql(stateCheckSql);
    }
        
    @Test
    public void checkThatCertificateIsNotRevoked() throws Exception {
        
        Certificate cert = new Certificate();
        cert.setCertificateId("certificate004");
        
        cert = processor.process(cert);
        
        assertNotNull(cert);
        
        assertFalse(cert.isRevoked());
    }
    
    @Test
    public void checkThatCertificateIsRevoked() throws Exception {
        
        Certificate cert = new Certificate();
        cert.setCertificateId("certificate003");
        
        cert = processor.process(cert);
        
        assertNotNull(cert);
        
        assertTrue(cert.isRevoked());
    }
}
