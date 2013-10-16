package se.inera.certificate.migration.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import se.inera.certificate.migration.model.OriginalCertificate;

public class ConvertCertificateXMLToJSONProcessorTest {

	private ConvertCertificateXMLToJSONProcessor processor;

	private LocalTestServer server = null;

	private HttpRequestHandler postHandler, serverTestHandler, error404Handler;

	private String serverUrl;

	@Before
	public void setUp() throws Exception {
	    
		// handle incoming posts to LocalTestServer
		postHandler = new HttpRequestHandler() {
			@Override
			public void handle(HttpRequest request, HttpResponse response,
					HttpContext ctx) throws HttpException, IOException {
				switch (request.getRequestLine().getMethod()) {
				case "POST":
					response.setStatusCode(200);
					StringEntity s = new StringEntity("blahonga");
					s.setContentType("application/json");
					response.setEntity(s);
					break;
				default:
					response.setStatusCode(405);
					response.setHeader("Allow", "POST");
					StringEntity body = new StringEntity(null);
					response.setEntity(body);
					break;
				}
			}
		};

		serverTestHandler = new HttpRequestHandler() {
			@Override
			public void handle(HttpRequest request, HttpResponse response,
					HttpContext ctx) throws HttpException, IOException {
				response.setStatusCode(200);
				StringEntity s = new StringEntity("Server is up");
				s.setContentType("text/plain");
				response.setEntity(s);
			}

		};

		error404Handler = new HttpRequestHandler() {
			@Override
			public void handle(HttpRequest request, HttpResponse response,
					HttpContext ctx) throws HttpException, IOException {
				response.setStatusCode(404);
			}
		};

		server = new LocalTestServer(null, null);
		server.register("/unmarshall", postHandler);
		server.register("/test", serverTestHandler);
		server.register("/*", error404Handler);
		server.start();

		serverUrl = "http:/" + server.getServiceAddress().getAddress() + ":"
				+ server.getServiceAddress().getPort();
		
	}

	@Test
	public void testServerAndClientConnectivity() throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(serverUrl + "/test");

		get.setHeader("Accept", "text/plain");
		get.setHeader("Content-Type", "text/plain");

		HttpResponse getResponse = client.execute(get);

		assertEquals(200, getResponse.getStatusLine().getStatusCode());

		String content = EntityUtils.toString(getResponse.getEntity());

		assertNotNull(content);
		assertEquals("Server is up", content);
	}

	@Test
	public void testServer404() throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(serverUrl + "/not_present");
		HttpResponse getResponse = client.execute(get);
		assertEquals(404, getResponse.getStatusLine().getStatusCode());

	}

	@Test
	public void testProcessorWithCert() throws Exception {
	    
	    processor = new ConvertCertificateXMLToJSONProcessor();
        processor.setConverterRestServiceUrl(serverUrl + "/unmarshall");
	    
		OriginalCertificate orgCert = createOriginalCert();

		String got = processor.convertOriginalCertificate(orgCert);

		assertNotNull(got);
		
	}
	
	@Test(expected = CertificateProcessingException.class)
    public void testProcessorWith404() throws Exception {
        
        processor = new ConvertCertificateXMLToJSONProcessor();
        processor.setConverterRestServiceUrl(serverUrl + "/gimmeAnError");
        
        OriginalCertificate orgCert = createOriginalCert();

        processor.convertOriginalCertificate(orgCert);        
    }

	@After
	public void tearDown() throws Exception {
		server.stop();

	}

	private OriginalCertificate createOriginalCert() throws IOException {

		OriginalCertificate orgCert = new OriginalCertificate();
		orgCert.setCertificateId("abc123");
		orgCert.setOriginalCertificateId(123);

		Resource fileRes = new ClassPathResource("data/maximalt-fk7263.xml");
		orgCert.setOriginalCertificate(FileUtils.readFileToByteArray(fileRes
				.getFile()));

		return orgCert;
	}
}
