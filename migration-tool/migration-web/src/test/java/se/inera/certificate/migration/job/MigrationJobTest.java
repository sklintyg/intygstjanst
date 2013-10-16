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

/**
 * Unit test for the data model migration job.
 * 
 * @author nikpet
 *
 */
@ContextConfiguration(locations = { "/test-application-context.xml", "/META-INF/spring/batch/jobs/jobs-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class MigrationJobTest {

    private static final int NBR_OF_CERTS_TO_LOAD = 50;

    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private Job migrationJob;

    @Autowired
    private CertificateDataInitialiser dataInitialiser;

    @Before
    public void initTestData() throws Exception {
                
        dataInitialiser.generateAndLoadCerts(NBR_OF_CERTS_TO_LOAD);
    }
    
    @Test
    public void testExecuteMigrationJob() throws Exception {
        final JobExecution jobExecution = jobLauncher.run(migrationJob, new JobParameters());
        assertEquals("Batch status not COMPLETED", BatchStatus.COMPLETED, jobExecution.getStatus());
    }
    
}
