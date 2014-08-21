package se.inera.certificate

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovy.xml.StreamingMarkupBuilder
import groovyx.net.http.HTTPBuilder

class RevokeraIntygFranXml {

    static void main(String[] args) {
        def props = new Properties()
        new File("webService.properties").withInputStream { stream ->
            props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        File infile = new File(args[0])
        int revokerade = 0
        int info = 0
        int error = 0
        infile.eachLine {line ->
            def xmlLine = new XmlSlurper().parseText(line)
            xmlLine.declareNamespace(qa: 'urn:riv:insuranceprocess:healthreporting:medcertqa:1',
                core: 'urn:riv:insuranceprocess:healthreporting:2',
                ns: 'urn:riv:insuranceprocess:healthreporting:SendMedicalCertificateQuestionResponder:1',
                s: 'http://schemas.xmlsoap.org/soap/envelope/')
            def question = xmlLine.'s:Body'.'ns:SendMedicalCertificateQuestion'.'ns:Question'
            def vardReferens = question.'ns:vardReferens-id'.text()
            def meddelandeText = question.'ns:fraga'.'qa:meddelandeText'.text()
            def avsant = question.'ns:avsantTidpunkt'.text()
            def hosPersonal = question.'ns:adressVard'.'qa:hosPersonal'
            def intygsId = question.'ns:lakarutlatande'.'qa:lakarutlatande-id'.text()
            def signering = question.'ns:lakarutlatande'.'qa:signeringsTidpunkt'.text()
            def patient = question.'ns:lakarutlatande'.'qa:patient'
            def builder = new StreamingMarkupBuilder()
            builder.encoding = "UTF-8"
            def doc = builder.bind {
                mkp.xmlDeclaration()
                mkp.declareNamespace(s: 'http://schemas.xmlsoap.org/soap/envelope/')
                's:Envelope' {
                    's:Header' {
                        mkp.declareNamespace(h: 'http://www.w3.org/2005/08/addressing')
                        'h:To'("2021005521")
                    }
                    's:Body' {
                        mkp.declareNamespace(core: 'urn:riv:insuranceprocess:healthreporting:2',
                        qa: 'urn:riv:insuranceprocess:healthreporting:medcertqa:1')
                        RevokeMedicalCertificateRequest(xmlns: 'urn:riv:insuranceprocess:healthreporting:RevokeMedicalCertificateResponder:1') {
                            revoke {
                                'vardReferens-id'(vardReferens)
                                if(meddelandeText) 'meddelande'(meddelandeText)
                                'avsantTidpunkt'(avsant)
                                'adressVard' {
                                    mkp.yield hosPersonal
                                }
                                'lakarutlatande' {
                                    'qa:lakarutlatande-id'(intygsId)
                                    'qa:signeringsTidpunkt'(signering)
                                    mkp.yield patient
                                }
                            }
                        }
                    }
                }
            }            
            def http = new HTTPBuilder( config.webService.revoke.URL , XML )
            http.request( POST, XML ) {
                headers."Content-Type" = "application/xml; charset=utf-8"
                headers."SOAPAction" = config.webService.revoke.SOAPAction
                body = doc.toString().replace('\\n', '\n')

                response.success = { resp, xml ->
                    if (xml.Body.RevokeMedicalCertificateResponse.result.resultCode == 'INFO') {
                        println "${intygsId} [INFO - ${xml.Body.RevokeMedicalCertificateResponse.result.infoText}]"
                        info++
                    } else if (xml.Body.RevokeMedicalCertificateResponse.result.resultCode == 'ERROR') {
                        println "${intygsId} [ERROR - ${xml.Body.RevokeMedicalCertificateResponse.result.errorText}]"
                        error++
                    } else {
                        revokerade++
                    }
                }
                response.failure = { resp, xml ->
                    println "failure: ${xml}"
                    error++
                }
            }
        }
        println "Revokerade: ${revokerade}"
        println "Fel: ${error}"
        println "Info: ${info}"

    }

}
