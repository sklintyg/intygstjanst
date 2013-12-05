package se.inera.certificate.mc2wc.batch.job;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import se.inera.certificate.mc2wc.dbunit.AbstractDbUnitSpringTest;

import com.github.springtestdbunit.annotation.DatabaseSetup;

@ContextConfiguration(locations = { "/spring/rest-client-test-context.xml",
        "/spring/rest-client-context.xml", "/spring/batch-infrastructure-context.xml",
        "/spring/beans-context.xml", "/spring/migration-job-context.xml" })
@DatabaseSetup({ "/data/certificate_dataset_25.xml" })
public class MigrationJobTest extends AbstractDbUnitSpringTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("migrationJob")
    private Job migrationJob;

    public MigrationJobTest() {
        // TODO Auto-generated constructor stub
    }

    @Test
    public void testRunMigrationJob() throws Exception {

        JobParameters params = new JobParameters();
        JobExecution execution = jobLauncher.run(migrationJob, params);

        assertEquals("Job did not complete OK", ExitStatus.COMPLETED, execution.getExitStatus());
    }
}
