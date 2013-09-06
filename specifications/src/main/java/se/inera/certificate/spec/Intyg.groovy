package se.inera.certificate.spec

import static groovyx.net.http.ContentType.JSON
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient

import org.springframework.core.io.ClassPathResource

import se.inera.certificate.spec.util.RestClientFixture

public class Intyg extends RestClientFixture {

    String personnr
    String utfärdat
	String giltigtFrån
	String giltigtTill
	String utfärdare
	String enhet
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
		utfärdare = "EnUtfärdare"
		enhet = "EnVårdEnhet"
		giltigtFrån = null
		giltigtTill = null
		template = null
		skickat = false
		rättat = false
	}
	
    public void execute() {
        def restClient = new RESTClient(baseUrl)
		if (!giltigtFrån) giltigtFrån = utfärdat
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
        certificate.'id'.extension = id

        // setting personnr in certificate XML
        certificate.patient.'id'.extension = personnr

		certificate.skapadAv.namn = utfärdare
		certificate.skapadAv.vardenhet.'id'.extension = enhet
		certificate.skapadAv.vardenhet.namn = enhet
		
        // setting the signing date, from date and to date
        certificate.signeringsDatum = utfärdat
        certificate.skickatDatum = utfärdat

		/*
        certificate.vardkontakter.each {
			it.vardkontaktstid.start = utfärdat
			it.vardkontaktstid.end = utfärdat
		}
		*/

        certificate.referenser.each { it.datum = utfärdat }

		/*
        certificate.aktivitetsbegransningar.arbetsformaga.arbetsformagaNedsattningar[0].each {
            it.varaktighetFrom = giltigtFrån
            it.varaktighetTom = giltigtTill
        }
        */
        JsonOutput.toJson(certificate)
    }
}
