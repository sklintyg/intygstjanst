package se.inera.certificate

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder
import groovyx.gpars.GParsPool
import groovyx.net.http.HTTPBuilder

import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.dbcp2.BasicDataSource

/**
 * Migrering av de intyg som drabbats av fält9-problematiken
 */
class Migrera {

    static void main(String[] args) {
        int numberOfThreads = args.length > 0 ? args[0] : 10
        long start = System.currentTimeMillis()
        def props = new Properties()
        new File("dataSource.properties").withInputStream { stream ->
            props.load(stream)
        }
        new File("webService.properties").withInputStream { stream ->
            props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        BasicDataSource dataSource =
            new BasicDataSource(driverClassName: config.dataSource.driver, url: config.dataSource.url,
                                username: config.dataSource.username, password: config.dataSource.password,
                                initialSize: numberOfThreads, maxTotal: numberOfThreads)
        def bootstrapSql = new Sql(dataSource)
        def certificateIds = bootstrapSql.rows("""
            select oc.CERTIFICATE_ID from ORIGINAL_CERTIFICATE oc
            where oc.DOCUMENT like '%motivering>%' 
            and oc.DOCUMENT not like '%prognosangivelse>%'
            and oc.CERTIFICATE_ID in (
                select cs.CERTIFICATE_ID from CERTIFICATE_STATE cs
                where cs.STATE = 'RECEIVED'
                and cs.TIMESTAMP < '2014-06-02 10:06:00'
            )""")
        bootstrapSql.close()
        
        def output = migrateCertificates(certificateIds, dataSource, numberOfThreads) 
        long end = System.currentTimeMillis()
        output.each {line ->
            if (line) println line
        }
        println "${output.size()} certificates migrated in ${end-start} milliseconds"
        
        Sql sql = new Sql(dataSource)
        int sent = 0
        int error = 0
        int info = 0
        certificateIds.each {
            def id = it.CERTIFICATE_ID
            def sentState = sql.rows("""
                select cs1.CERTIFICATE_ID from CERTIFICATE_STATE cs1
                left join CERTIFICATE_STATE cs2 on cs1.CERTIFICATE_ID = cs2.CERTIFICATE_ID
                where cs1.CERTIFICATE_ID = :id
                and cs1.STATE = 'RECEIVED' 
                and cs2.STATE = 'SENT'
                and TIMESTAMPDIFF(SECOND, cs1.TIMESTAMP, cs2.TIMESTAMP) > 1
                and cs1.CERTIFICATE_ID not in (
                    select cs3.CERTIFICATE_ID from CERTIFICATE_STATE cs3
                    where cs3.STATE = 'CANCELLED')
                """, [id : id])
            if (sentState) {
                def row = sql.firstRow('select DOCUMENT from ORIGINAL_CERTIFICATE where CERTIFICATE_ID = :id' , [id : id])
                def sendMessage = buildSendMessage(new String(row.DOCUMENT, 'UTF-8'), id)
                def http = new HTTPBuilder( config.webService.send.URL , XML )
                http.request( POST, XML ) {
                    headers."Content-Type" = "application/xml; charset=utf-8"
                    headers."SOAPAction" = config.webService.send.SOAPAction
                    body = sendMessage.toString().replace('\\n', '\n')
    
                    response.success = { resp, xmlResponse ->
                        if (xmlResponse.Body.SendMedicalCertificateResponse.result.resultCode == 'INFO') {
                            println "${id} [INFO - ${xmlResponse.Body.SendMedicalCertificateResponse.result.infoText}]"
                            info++
                            sent++
                        } else if (xmlResponse.Body.SendMedicalCertificateResponse.result.resultCode == 'ERROR') {
                            println "${id} [ERROR - ${xmlResponse.Body.SendMedicalCertificateResponse.result.errorText}]"
                            error++
                        } else {
                            sent++
                        }
                    }
                    response.failure = { resp, xmlResponse ->
                        println "failure: ${xmlResponse}"
                        error++
                    }
                }
                Thread.sleep(1)
            }
        }
        sql.close()
        println "$sent certificates sent to FK, $info with info messages, $error errors"
    }

    static def migrateCertificates(def certificateIds, def dataSource, int numberOfThreads) {
        final AtomicInteger count = new AtomicInteger(0)
        def results
        GParsPool.withPool(numberOfThreads) {
            results = certificateIds.collectParallel {
                StringBuffer result = new StringBuffer()
                def id = it.CERTIFICATE_ID
                Sql sql = new Sql(dataSource)
                def row = sql.firstRow( 'select DOCUMENT from ORIGINAL_CERTIFICATE where CERTIFICATE_ID = :id' , [id : id])
                result << "$id should be converted"
                // def document = new JsonSlurper().parseText(new String(row.DOCUMENT, 'UTF-8'))
                // sql.execute('update CERTIFICATE set DOCUMENT = :document where ID = :id' , [document: updatedDocument, id : id])
                sql.close()
                int current = count.addAndGet(1)
                if (current % 100 == 0) {
                    println current
                }
                result.toString()
            }
        }
        results
    }
    
    static def buildSendMessage(def registerXml, def id) {
        def xml = new XmlSlurper().parseText(registerXml)
        xml.declareNamespace(ns1: 'urn:riv:insuranceprocess:healthreporting:mu7263:3',
            qa: 'urn:riv:insuranceprocess:healthreporting:medcertqa:1',
            ns2: 'urn:riv:insuranceprocess:healthreporting:2',
            ns3: 'urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3',
            s: 'http://schemas.xmlsoap.org/soap/envelope/')
        def intyg = xml.'ns3:lakarutlatande'
        def avsant = intyg.'ns1:skickatDatum'.text()
        def signering = intyg.'ns1:signeringsdatum'.text()
        def patientId = intyg.'ns1:patient'.'ns2:person-id'
        def patientNamn = intyg.'ns1:patient'.'ns2:fullstandigtNamn'.text()
        def hosPersonalId = intyg.'ns1:skapadAvHosPersonal'.'ns2:personal-id'
        def hosPersonalNamn = intyg.'ns1:skapadAvHosPersonal'.'ns2:fullstandigtNamn'.text()
        def enhet = intyg.'ns1:skapadAvHosPersonal'.'ns2:enhet'
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
                    SendMedicalCertificateRequest(xmlns: 'urn:riv:insuranceprocess:healthreporting:SendMedicalCertificateResponder:1') {
                        send {
                            'vardReferens-id'("Migrera-fält9-$id")
                            'avsantTidpunkt'(avsant)
                            'adressVard' {
                                'qa:hosPersonal' {
                                    mkp.yield hosPersonalId
                                    'core:fullstandigtNamn'(hosPersonalNamn)
                                    mkp.yield enhet
                                }
                            }
                            'lakarutlatande' {
                                'qa:lakarutlatande-id'(id)
                                'qa:signeringsTidpunkt'(signering)
                                'qa:patient' {
                                     mkp.yield patientId
                                     'core:fullstandigtNamn'(patientNamn)
                                }
                            }
                        }
                    }
                }
            }
        }
        doc.toString()
    }
}
