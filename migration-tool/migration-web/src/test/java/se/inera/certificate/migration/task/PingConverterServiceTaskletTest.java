package se.inera.certificate.migration.task;

import static org.junit.Assert.assertEquals;

import org.apache.http.localserver.LocalTestServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.repeat.RepeatStatus;

import se.inera.certificate.migration.processors.FatalCertificateProcessingException;
import se.inera.certificate.migration.testutils.http.IntygHttpRequestHandler;
import se.inera.certificate.migration.testutils.http.IntygHttpRequestHandler.IntygHttpRequestHandlerMode;

public class PingConverterServiceTaskletTest {

    private LocalTestServer server = null;

    private String serverUrl = null;

    @Before
    public void setup() throws Exception {
        server = new LocalTestServer(null, null);
        server.register("/unmarshall", new IntygHttpRequestHandler(IntygHttpRequestHandlerMode.HANDLE_POST));
        server.register("/notfound", new IntygHttpRequestHandler(IntygHttpRequestHandlerMode.NOT_FOUND_404));

        server.start();

        this.serverUrl = "http:/" + server.getServiceAddress().getAddress() + ":" + server.getServiceAddress().getPort();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testPingNormal() throws Exception {
        PingConverterServiceTasklet pingTask = new PingConverterServiceTasklet(serverUrl.concat("/unmarshall"));
        RepeatStatus status = pingTask.execute(null, null);
        assertEquals(RepeatStatus.FINISHED, status);
    }

    @Test(expected = FatalCertificateProcessingException.class)
    public void testPingWith404() throws Exception {
        PingConverterServiceTasklet pingTask = new PingConverterServiceTasklet(serverUrl.concat("/notfound"));
        pingTask.execute(null, null);
    }

    @Test(expected = FatalCertificateProcessingException.class)
    public void testPingWithWrongUrl() throws Exception {
        PingConverterServiceTasklet pingTask = new PingConverterServiceTasklet("http://wrong");
        pingTask.execute(null, null);
    }
}
