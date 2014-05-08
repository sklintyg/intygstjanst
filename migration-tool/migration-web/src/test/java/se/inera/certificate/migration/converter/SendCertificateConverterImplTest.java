package se.inera.certificate.migration.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.migration.model.Certificate;
import se.inera.certificate.model.Patient;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;

public class SendCertificateConverterImplTest {

	private SendCertificateConverterImpl converterImpl;
	
	@Before
	public void setup() {
		converterImpl = new SendCertificateConverterImpl();
		converterImpl.setObjectMapper(new CustomObjectMapper());
	}
	
	@Test
	public void testConvertToSendRequest() throws Exception {
		Certificate cert = makeCert("/data/intyg-1-content.json");
		
		SendMedicalCertificateRequestType sendRequest = converterImpl.convertToSendRequest(cert);
		
		assertNotNull(sendRequest);
		assertNotNull(sendRequest.getSend());
		assertNotNull(sendRequest.getSend().getVardReferensId());
		assertNotNull(sendRequest.getSend().getAvsantTidpunkt());
		
		assertNotNull(sendRequest.getSend().getAdressVard());
		assertNotNull(sendRequest.getSend().getAdressVard().getHosPersonal());
		assertNotNull(sendRequest.getSend().getAdressVard().getHosPersonal().getPersonalId());
		assertNotNull(sendRequest.getSend().getAdressVard().getHosPersonal().getEnhet());
		assertNotNull(sendRequest.getSend().getAdressVard().getHosPersonal().getEnhet().getEnhetsId());
		assertNotNull(sendRequest.getSend().getAdressVard().getHosPersonal().getEnhet().getEnhetsnamn());
		
		assertNotNull(sendRequest.getSend().getLakarutlatande());
		assertNotNull(sendRequest.getSend().getLakarutlatande().getLakarutlatandeId());
		assertNotNull(sendRequest.getSend().getLakarutlatande().getSigneringsTidpunkt());
		assertNotNull(sendRequest.getSend().getLakarutlatande().getPatient());
		assertNotNull(sendRequest.getSend().getLakarutlatande().getPatient().getPersonId());
		assertNotNull(sendRequest.getSend().getLakarutlatande().getPatient().getFullstandigtNamn());
	}
		
	@Test
	public void testJoinNames() {
		
		Patient patient = new Patient();
		patient.setEfternamn("Davidsson Eriksson");
		patient.getFornamn().addAll(Arrays.asList("Arne", "Bengt", "Carl"));
		
		String fullName = SendCertificateConverterImpl.joinNames(patient);
		
		assertEquals("Arne Bengt Carl Davidsson Eriksson", fullName);
	}
	
	private Certificate makeCert(String fileName) throws IOException {
		
		Certificate cert = new Certificate();
		cert.setOriginalCertificateId(123);
		cert.setCertificateId("abcd1234");
		cert.setSignedDate(LocalDateTime.now());
		String json = readJson(fileName);
		cert.setCertificateJson(json);
		
		return cert;
	}
	
	private String readJson(String fileName) throws IOException {
		Resource resource = new ClassPathResource(fileName);
		return FileUtils.readFileToString(resource.getFile(), "UTF-8");
	}
	
	
}
