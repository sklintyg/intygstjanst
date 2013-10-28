package se.inera.certificate.migration.job;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.certificate.migration.testutils.CertificateDataInitialiser;
import se.inera.certificate.migration.testutils.dao.Cert;
import se.inera.certificate.migration.testutils.dao.CertTestDao;
import se.inera.certificate.migration.testutils.http.IntygHttpRequestHandler;
import se.inera.certificate.migration.testutils.http.IntygHttpRequestHandler.IntygHttpRequestHandlerMode;

/**
 * Unit test for the data model migration job.
 * 
 * @author nikpet
 *
 */
@ContextConfiguration(locations = { "/test-application-context.xml", "/META-INF/spring/batch/jobs/jobs-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class MigrationJobTest {

    private static final int NBR_OF_CERTS_TO_LOAD = 50;

    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private Job migrationJob;

    @Autowired
    private CertificateDataInitialiser dataInitialiser;
    
    @Autowired
    private CertTestDao certTestDao;

    private LocalTestServer server = null;

    private String serverUrl;
    
    @Before
    public void initTestData() throws Exception {
        
        server = new LocalTestServer(null, null);
        server.register("/unmarshall", new IntygHttpRequestHandler(IntygHttpRequestHandlerMode.HANDLE_POST));
        server.register("/notfound", new IntygHttpRequestHandler(IntygHttpRequestHandlerMode.NOT_FOUND_404));
        server.start();
                
        dataInitialiser.generateAndLoadCerts(NBR_OF_CERTS_TO_LOAD);
        
        serverUrl = "http:/" + server.getServiceAddress().getAddress() + ":"
                + server.getServiceAddress().getPort();
    }
    
    @Test
    @DirtiesContext
    public void testExecuteMigrationJobWithWrongUrl() throws Exception {
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("CONVERTER_SERVICE_URL", "http://localhost:9999/blahonga");
        
        final JobExecution jobExecution = jobLauncher.run(migrationJob, builder.toJobParameters());
        assertEquals("Batch status should be FAILED", BatchStatus.FAILED, jobExecution.getStatus());
    }
    
    @Test
    @DirtiesContext
    public void testExecuteMigrationJobWith404Response() throws Exception {
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("CONVERTER_SERVICE_URL", serverUrl + "/notfound");
        
        final JobExecution jobExecution = jobLauncher.run(migrationJob, builder.toJobParameters());
        assertEquals("Batch status should be FAILED", BatchStatus.FAILED, jobExecution.getStatus());
    }
    
    @Test
    @DirtiesContext
    public void testExecuteMigrationJob() throws Exception {
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("CONVERTER_SERVICE_URL", serverUrl + "/unmarshall");
        
        final JobExecution jobExecution = jobLauncher.run(migrationJob, builder.toJobParameters());
        assertEquals("Batch status should be COMPLETED", BatchStatus.COMPLETED, jobExecution.getStatus());
        
        assertEquals(NBR_OF_CERTS_TO_LOAD, certTestDao.countOriginalCertsWithCertificateIDs());
    }
}
