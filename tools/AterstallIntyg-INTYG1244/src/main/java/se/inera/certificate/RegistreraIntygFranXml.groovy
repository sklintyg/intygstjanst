package se.inera.certificate

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class RegistreraIntygFranXml {

    static boolean CONTAINS_ENVELOPE = true
    
    static void main(String[] args) {
        def exkludera = [] as Set
        if (args.length >= 2) {
            new File(args[1]).eachLine {
                exkludera << it
            }
        }
        def props = new Properties()
        new File("webService.properties").withInputStream { stream ->
            props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        def envelopePrefix = '<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/"><s:Header><h:To xmlns:h="http://www.w3.org/2005/08/addressing" xmlns="http://www.w3.org/2005/08/addressing" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">2021005521</h:To></s:Header><s:Body xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">'
        def envelopeSuffix = '</s:Body></s:Envelope>'
        File infile = new File(args[0])
        int exkluderade = 0
        int skickade = 0
        int info = 0
        int error = 0
        infile.eachLine {line ->
            String intygsId
            def xmlLine = new XmlSlurper().parseText(line)
            xmlLine.declareNamespace(ns1: 'urn:riv:insuranceprocess:healthreporting:mu7263:3',
                ns2: 'urn:riv:insuranceprocess:healthreporting:2',
                ns3: 'urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3',
                s: 'http://schemas.xmlsoap.org/soap/envelope/')
            if (CONTAINS_ENVELOPE) {
                intygsId = xmlLine.'s:Body'.'ns3:RegisterMedicalCertificate'.'ns3:lakarutlatande'.'ns1:lakarutlatande-id'.text()
            } else {
                intygsId = xmlLine.'ns3:lakarutlatande'.'ns1:lakarutlatande-id'.text()
            }
            boolean found = exkludera.contains(intygsId)
            if (found) {
                println "exkluderar $intygsId"
                exkluderade++
            } else {
                def http = new HTTPBuilder( config.webService.register.URL , XML )
                http.request( POST, XML ) {
                    headers."Content-Type" = "application/xml; charset=utf-8"
                    headers."SOAPAction" = config.webService.register.SOAPAction
                    if (CONTAINS_ENVELOPE) {
                        body = line.replace('\\n', '\n')
                    } else {
                        def xmlProlog = line.substring(line.indexOf('<?'), line.indexOf('?>') + 2)
                        def xmlBody = line.substring(line.indexOf('?>') + 2)
                        body = "${xmlProlog}${envelopePrefix}${xmlBody.replace('\\n', '\n')}${envelopeSuffix}"
                    }
    
                    response.success = { resp, xml ->
                        if (xml.Body.RegisterMedicalCertificateResponse.result.resultCode == 'INFO') {
                            println "${intygsId} [INFO - ${xml.Body.RegisterMedicalCertificateResponse.result.infoText}]"
                            info++
                        } else if (xml.Body.RegisterMedicalCertificateResponse.result.resultCode == 'ERROR') {
                            println "${intygsId} [ERROR - ${xml.Body.RegisterMedicalCertificateResponse.result.errorText}]"
                            error++
                        } else {
                            exkludera << intygsId
                            skickade++
                        }
                    }
                    response.failure = { resp, xml ->
                        println "failure: ${xml}"
                            error++
                    }
                }
            }
        }
        println "Exkluderade: ${exkluderade}"
        println "Skickade: ${skickade}"
        println "Fel: ${error}"
        println "Info: ${info}"
    }

}
