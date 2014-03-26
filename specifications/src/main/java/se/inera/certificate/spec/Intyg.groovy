package se.inera.certificate.spec
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.core.io.ClassPathResource
import se.inera.certificate.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

public class Intyg extends RestClientFixture {

    String personnr
    String utfärdat
	String giltigtFrån
	String giltigtTill
    String utfärdarId
    String utfärdare
    String enhetsId = "1.2.3"
	String enhet
    String vårdgivarId
    String typ
    String id
    String idTemplate
	String mall = "M"
    int from
    int to
	private boolean skickat
	private boolean rättat
	
	private String template
	
	public void setSkickat(String value) {
		if (value != null && value.equalsIgnoreCase("ja")) {
			skickat = true
		} else {
			skickat = false
		}
	}

	public void setRättat(String value) {
		if (value != null && value.equalsIgnoreCase("ja")) {
			rättat = true
		} else {
			rättat = false
		}
	}

	public void reset() {
		mall = "M"
		utfärdarId = "EttUtfärdarId"
        utfärdare = "EnUtfärdare"
        enhetsId = "1.2.3"
		enhet = null
		giltigtFrån = null
		giltigtTill = null
		template = null
		skickat = false
		rättat = false
	}
	
    public void execute() {
        def restClient = createRestClient()
        if (!giltigtFrån) giltigtFrån = utfärdat
        if (!enhet) enhet = enhetsId
		if (!giltigtTill) giltigtTill = new Date().parse("yyyy-MM-dd", utfärdat).plus(14).format("yyyy-MM-dd")
		if (from && to && !idTemplate) {
			template = "test-${personnr}-intyg-%1\$s"
		} else if (idTemplate) {
			template = idTemplate
		}
        for (int day in from..to) {
            if (template) {
                id = String.format(template, day, utfärdat, personnr, typ)
            }
            restClient.post(
                    path: 'certificate',
                    body: certificateJson(),
                    requestContentType: JSON
                    )
            utfärdat = new Date().parse("yyyy-MM-dd", utfärdat).plus(1).format("yyyy-MM-dd")
        }
    }

    private certificateJson() {
		def stateList = [[state:"RECEIVED", target:"MI", timestamp:"2013-08-05T14:30:03.227"]]
		if (skickat)
			stateList << [state:"SENT", target:"MI", timestamp:"2013-08-05T14:31:03.227"]
		if (rättat)
			stateList << [state:"CANCELLED", target:"MI", timestamp:"2013-08-05T14:32:03.227"]
        [id:String.format(id, utfärdat),
            type:typ,
            civicRegistrationNumber:personnr,
            signedDate:utfärdat,
            signingDoctorName: utfärdare,
            validFromDate:giltigtFrån,
            validToDate:giltigtTill,
            careUnitId: (enhetsId) ? enhetsId : "1.2.3",
            careUnitName: enhet,
			states: stateList,
            document: document()
        ]
    }

    protected document() {
        if ((typ == 'fk7263')||(typ == 'FK7263')) {
            "\"" + document("fk7263") + "\""
        }
        else {
            "\"" + document("rli") + "\""
        }
    }

    protected document(typ) {
        // slurping the FK7263 template
        def certificate = new JsonSlurper().parse(new InputStreamReader(new ClassPathResource("${typ}_${mall}_template.json").getInputStream()))

        // setting the certificate ID
        certificate.'id'.root = id
        certificate.'id'.extension = id

        // setting personnr in certificate XML
        certificate.patient.'id'.extension = personnr

        if (utfärdarId) certificate.skapadAv.'id'.extension = utfärdarId
		if (utfärdare) certificate.skapadAv.namn = utfärdare
		if (enhetsId) certificate.skapadAv.vardenhet.'id'.extension = enhetsId
		if (enhet) certificate.skapadAv.vardenhet.namn = enhet

        if (vårdgivarId) certificate.skapadAv.vardenhet.vardgivare.'id'.extension = vårdgivarId
		
        // setting the signing date, from date and to date
        certificate.signeringsdatum = utfärdat
        certificate.skickatdatum = utfärdat


        certificate.vardkontakter.each { it.vardkontaktstid.from = utfärdat; it.vardkontaktstid.tom = utfärdat }
        certificate.referenser.each { it.datum = utfärdat }
        def observationsperioder = []
        certificate.observationer.each {observation ->
            if (observation?.observationskod?.code == "302119000") {
                observationsperioder << observation.observationsperiod
            }
        }
        observationsperioder.eachWithIndex {period, index ->
            period.from = new Date().parse("yyyy-MM-dd", giltigtFrån).plus(index * 2).format("yyyy-MM-dd")
            if (index + 1 < observationsperioder.size()) {
                period.tom = new Date().parse("yyyy-MM-dd", giltigtFrån).plus((index * 2) + 1).format("yyyy-MM-dd")
            } else {
                period.tom = giltigtTill
            }
        }
        certificate.validFromDate = giltigtFrån
        certificate.validToDate = giltigtTill
        
        JsonOutput.toJson(certificate)
    }
}
