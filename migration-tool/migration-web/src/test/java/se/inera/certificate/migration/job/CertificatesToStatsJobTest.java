package se.inera.certificate.migration.job;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import se.inera.certificate.migration.testutils.dbunit.AbstractDbUnitSpringTest;
import se.inera.certificate.migration.testutils.jms.TestQueueInspector;

import com.github.springtestdbunit.annotation.DatabaseSetup;

@ContextConfiguration(locations = { "/application-context.xml", "/META-INF/spring/batch/jobs/certificates-to-statistics-job.xml" })
@DatabaseSetup("/data/certificate-dataset.xml")
@ActiveProfiles({"dev","unit-testing"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class CertificatesToStatsJobTest extends AbstractDbUnitSpringTest {

    private String queueName = "CERTIFICATE.QUEUE";

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
                
        boolean running = jobExecution.isRunning();
        
        while(running) {
            running = jobExecution.isRunning();
            Thread.sleep(250);
        }
        
        assertEquals("Batch status should be COMPLETED", BatchStatus.COMPLETED, jobExecution.getStatus());
        
        Long queueSize = inspector.getQueueSize(queueName);
        assertEquals("Queue should contain 5 create and 1 revoke messages", new Long(6L), queueSize);
    }

}
