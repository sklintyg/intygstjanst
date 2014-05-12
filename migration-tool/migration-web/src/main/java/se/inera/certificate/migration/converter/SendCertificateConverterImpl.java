package se.inera.certificate.migration.converter;

import iso.v21090.dt.v1.II;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;

import riv.insuranceprocess.healthreporting.medcertqa._1.LakarutlatandeEnkelType;
import riv.insuranceprocess.healthreporting.medcertqa._1.VardAdresseringsType;
import se.inera.certificate.migration.model.Certificate;
import se.inera.certificate.model.HosPersonal;
import se.inera.certificate.model.Id;
import se.inera.certificate.model.Patient;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.Vardenhet;
import se.inera.certificate.model.Vardgivare;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SendCertificateConverterImpl implements SendCertificateConverter {

	@Autowired
	private ObjectMapper objectMapper;

	/* (non-Javadoc)
	 * @see se.inera.certificate.migration.converter.SendCertificateConverter#convertToSendRequest(se.inera.certificate.migration.model.Certificate)
	 */
	@Override
	public SendMedicalCertificateRequestType convertToSendRequest(
			Certificate cert) {

		Utlatande utlatande = convertJsonToUtlatande(cert.getCertificateJsonAsString());

		SendType sendType = convertUtlatandeToSendType(utlatande, cert);

		SendMedicalCertificateRequestType requestType = new SendMedicalCertificateRequestType();
		requestType.setSend(sendType);

		return requestType;
	}

	private SendType convertUtlatandeToSendType(Utlatande utlatande, Certificate cert) {

		PatientType patientType = new PatientType();
		patientType.setFullstandigtNamn(joinNames(utlatande.getPatient()));
		patientType.setPersonId(convertToII(utlatande.getPatient().getId()));

		LakarutlatandeEnkelType utlatandeType = new LakarutlatandeEnkelType();
		utlatandeType.setLakarutlatandeId(cert.getCertificateId());
		utlatandeType.setSigneringsTidpunkt(cert.getSignedDate());
		utlatandeType.setPatient(patientType);
		
		HosPersonal hosPersonal = utlatande.getSkapadAv();
		
		EnhetType hosEnhet = buildEnhet(hosPersonal.getVardenhet());
		
		HosPersonalType hosPers = new HosPersonalType();
		hosPers.setFullstandigtNamn(hosPersonal.getNamn());
		hosPers.setPersonalId(convertToII(hosPersonal.getId()));
		hosPers.setEnhet(hosEnhet);
		
		VardAdresseringsType vardAddrType = new VardAdresseringsType();
		vardAddrType.setHosPersonal(hosPers);

		SendType sendType = new SendType();
		sendType.setAdressVard(vardAddrType);
		sendType.setAvsantTidpunkt(LocalDateTime.now());
		sendType.setVardReferensId("123"); // Ska vara vad??
		sendType.setLakarutlatande(utlatandeType);

		return sendType;
	}

	private EnhetType buildEnhet(Vardenhet vardenhet) {
		
		EnhetType enhetType = new EnhetType();
		enhetType.setEnhetsId(convertToII(vardenhet.getId()));
		enhetType.setEnhetsnamn(vardenhet.getNamn());
		
		Vardgivare vardgivare = vardenhet.getVardgivare();
		
		VardgivareType vardgivareType = new VardgivareType();
		vardgivareType.setVardgivarnamn(vardgivare.getNamn());
		vardgivareType.setVardgivareId(convertToII(vardgivare.getId()));
		
		enhetType.setVardgivare(vardgivareType);
		
		return enhetType;
	}

	static String joinNames(Patient patient) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.join(patient.getFornamn().iterator(), " "));
		sb.append(" ").append(patient.getEfternamn());
		
		return StringUtils.stripToEmpty(sb.toString());
	}

	private Utlatande convertJsonToUtlatande(String certificateJson) {
		try {
            return objectMapper.readValue(certificateJson, Utlatande.class);
        } catch (IOException e) {
            throw new RuntimeException();
        }
	}

	private static II convertToII(Id id) {
		
		II ii = new II();
		ii.setExtension(id.getExtension());
		ii.setRoot(id.getRoot());
		
		return ii;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
}
