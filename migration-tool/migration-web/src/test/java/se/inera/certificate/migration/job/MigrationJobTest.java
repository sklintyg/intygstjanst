package se.inera.certificate.migration.job;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.certificate.migration.testutils.CertificateDataInitialiser;
import se.inera.certificate.migration.testutils.dao.Cert;

@ContextConfiguration(locations = { "/test-application-context.xml", "/META-INF/spring/batch/jobs/jobs-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class MigrationJobTest {

    private static final String ORIGINAL_CERTIFICATE_FILEPATH = "/data/maximalt-fk7263.xml";

    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private Job migrationJob;

    @Autowired
    private CertificateDataInitialiser dataInitialiser;

    @Before
    public void initTestData() throws Exception {
        
        Cert c1 = new Cert("aaa00000001", "19121212-0001"); 
        Cert c2 = new Cert("aaa00000002", "19121212-0002");
        Cert c3 = new Cert("aaa00000003", "19121212-0003");
        
        List<Cert> certs = Arrays.asList(c1, c2, c3);
        
        dataInitialiser.loadCerts(certs, ORIGINAL_CERTIFICATE_FILEPATH);
    }
    
    @Test
    public void testExecuteMigrationJob() throws Exception {
        final JobExecution jobExecution = jobLauncher.run(migrationJob, new JobParameters());
        assertEquals("Batch status not COMPLETED", BatchStatus.COMPLETED, jobExecution.getStatus());
    }
    
}
