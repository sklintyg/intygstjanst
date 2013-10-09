package se.inera.certificate.migration.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "/test-application-context.xml", "/META-INF/spring/batch/jobs/jobs-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class MigrationJobTest {
    
    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private Job migrationJob;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void testExecuteMigrationJob() throws Exception {
        final JobExecution jobExecution = jobLauncher.run(migrationJob, new JobParameters());
        assertEquals("Batch status not COMPLETED", BatchStatus.COMPLETED, jobExecution.getStatus());
    }
    
}
