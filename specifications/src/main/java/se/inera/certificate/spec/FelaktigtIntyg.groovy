package se.inera.certificate.spec

import static groovyx.net.http.ContentType.JSON
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient

import org.springframework.core.io.ClassPathResource

import se.inera.certificate.spec.util.RestClientFixture

public class FelaktigtIntyg extends Intyg {

    String felaktigtPersonnr
	
	protected document(typ) {
		// slurping the FK7263 template
		def certificate = new JsonSlurper().parseText(super.document(typ))

		// setting personnr in certificate XML
		certificate.patient.'id'.extension = felaktigtPersonnr

		JsonOutput.toJson(certificate)
	}

}
