package se.inera.certificate.migration.job;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.http.localserver.LocalTestServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.certificate.migration.testutils.dao.CertTestDao;
import se.inera.certificate.migration.testutils.dbunit.AbstractDbUnitSpringTest;
import se.inera.certificate.migration.testutils.http.IntygHttpRequestHandler;
import se.inera.certificate.migration.testutils.http.IntygHttpRequestHandler.IntygHttpRequestHandlerMode;

import com.github.springtestdbunit.annotation.DatabaseSetup;

/**
 * Unit test for the data model migration job.
 * 
 * @author nikpet
 * 
 */
@ContextConfiguration(locations = { "/application-context.xml",
        "/META-INF/spring/batch/jobs/send-certs-to-fk-job.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"dev","unit-testing"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@DatabaseSetup({"/data/intyg_dataset_falt9.xml"})
public class ReSendCertificatesToFKJobTest extends AbstractDbUnitSpringTest {

	private Logger logger = LoggerFactory.getLogger(ReSendCertificatesToFKJobTest.class);
	
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("reSendCertificatesToFKJob")
    private Job reSendCertJob;

    @Autowired
    private CertTestDao certTestDao;
   
    @Test
    public void testExecuteMigrationJob() throws Exception {
        JobParametersBuilder builder = new JobParametersBuilder();

        final JobExecution jobExecution = jobLauncher.run(reSendCertJob, builder.toJobParameters());

        await().atMost(10, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                logger.debug("exitStatus: {}", jobExecution.getExitStatus());
                return jobExecution.getExitStatus().equals(ExitStatus.COMPLETED);
            }
        });

        assertEquals("Batch status should be COMPLETED", BatchStatus.COMPLETED, jobExecution.getStatus());
    }
}
