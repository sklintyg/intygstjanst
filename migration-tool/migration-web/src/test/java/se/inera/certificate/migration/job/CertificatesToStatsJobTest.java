package se.inera.certificate.migration.job;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;

import se.inera.certificate.migration.testutils.dbunit.AbstractDbUnitSpringTest;
import se.inera.certificate.migration.testutils.jms.TestQueueInspector;

import com.github.springtestdbunit.annotation.DatabaseSetup;

@ContextConfiguration(locations = { "/test-application-context.xml", "/spring/batch-context.xml",
        "/spring/certificates-to-statistics-context.xml", "/spring/test-jms-context.xml", "/META-INF/spring/batch/jobs/certificates-to-statistics-job.xml" })
@DatabaseSetup("/data/certificate-dataset.xml")
public class CertificatesToStatsJobTest extends AbstractDbUnitSpringTest {
    
    @Value("${activemq.test.queue}")
    private String queueName;
        
    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private Job certsToStatsJob;
    
    @Autowired
    private TestQueueInspector inspector;
    
    public CertificatesToStatsJobTest() {
        
    }

    @Test
    public void runJob() throws Exception {
        
        JobParametersBuilder builder = new JobParametersBuilder();
        
        final JobExecution jobExecution = jobLauncher.run(certsToStatsJob, builder.toJobParameters());
        assertEquals("Batch status should be COMPLETED", BatchStatus.COMPLETED, jobExecution.getStatus());
        
        //TODO: Enable when getting JMX to work properly
        //Long queueSize = inspector.getQueueSize(queueName);
        //assertEquals(new Long(5L), queueSize);
    }
    
}
