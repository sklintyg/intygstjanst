package se.inera.certificate.tools.falt9

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovyx.gpars.GParsPool
import groovyx.net.http.HTTPBuilder

import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.dbcp2.BasicDataSource
import org.apache.http.util.EntityUtils
import org.joda.time.LocalDateTime

/**
 * Migrering av de intyg som drabbats av fält9-problematiken
 */
class Migrera {

    static void main(String[] args) {

        println "- Starting Fält 9 - Certificate migration"
        
        int numberOfThreads = args.length > 0 ? args[0] : 5
        long start = System.currentTimeMillis()
        def props = new Properties()
        new File("dataSource.properties").withInputStream { stream ->
            props.load(stream)
        }
        new File("webService.properties").withInputStream { stream ->
            props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        
        def theDate = LocalDateTime.parse(config.dataSource.enddate)
        println "- Using date: $theDate"
        
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
                and cs.TIMESTAMP < :date
            )""", [date : theDate.toDate()])
        bootstrapSql.close()
                
        println "- ${certificateIds.size()} candidates found"
        
        final AtomicInteger totalCount = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)
        final AtomicInteger recoverCount = new AtomicInteger(0)
        
        def converterUrl = config.webService.convert.URL
        
        def results
        
        GParsPool.withPool(numberOfThreads) {
            
            results = certificateIds.collectParallel {
                StringBuffer result = new StringBuffer()
                def id = it.CERTIFICATE_ID
                Sql sql = new Sql(dataSource)
                def row = sql.firstRow( 'select DOCUMENT from ORIGINAL_CERTIFICATE where CERTIFICATE_ID = :id' , [id : id])
                                                                                    
                def originalDocument = new String(row.DOCUMENT, 'UTF-8')
                                
                def updatedDocument = null
                
                def http = new HTTPBuilder(converterUrl);
                
                http.request(POST, XML) {
                    headers.'Accept' = 'application/json'
                    headers.'ContentEncoding' = 'UTF-8'
                    body = originalDocument
                    response.success = { resp ->
                        updatedDocument = EntityUtils.toString(resp.entity, 'UTF-8')
                    }
                    response.failure = { resp ->
                        def msg = EntityUtils.toString(resp.entity, 'UTF-8')
                        result << "${id};${resp.status};${msg}"
                        errorCount.incrementAndGet()
                        result << "\nTrying alternative conversion strategy for ${id}"
                        try {
                            def originalJson = new String(sql.firstRow( 'select DOCUMENT from CERTIFICATE where ID = :id' , [id : id]).DOCUMENT, 'UTF-8')
                            updatedDocument = transformJsonFromXml(originalDocument, originalJson)
                            recoverCount.incrementAndGet()
                            result << "\nAlternative conversion strategy successful for ${id}"
                        } catch (Throwable t) {
                            result << "\nAlternative conversion strategy for ${id} failed: ${t.message}"
                        }
                    }
                }
                
                http.shutdown()
                
                if (updatedDocument) {
                    sql.execute('update CERTIFICATE set DOCUMENT = :document where ID = :id' , [document: updatedDocument.getBytes('UTF-8'), id : id])
                }
                
                sql.close()
                
                int current = totalCount.incrementAndGet()
                                
                if (current % 1000 == 0) {
                    println "- ${current} certificates processed, ${errorCount} errors, ${recoverCount} errors recovered"
                }
                result.toString()
            }
        }
         
        long end = System.currentTimeMillis()
        
        println "- Done! ${totalCount} certificates processed with ${errorCount} errors and ${recoverCount} errors recovered in ${(int)((end-start) / 1000)} seconds"
        
        if (results.size() > 0) {
            println " "
            println "id;status;message"
            results.each { line ->
                if (line) println line
            }
        }
        
    }
    
    static def transformJsonFromXml(String xml, String json) {
        def registerMedicalCertificate = new XmlSlurper().parseText(xml)
        registerMedicalCertificate.declareNamespace(ns1: 'urn:riv:insuranceprocess:healthreporting:mu7263:3',
            ns2: 'urn:riv:insuranceprocess:healthreporting:2',
            ns3: 'urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3')
        def motivering = registerMedicalCertificate.'ns3:lakarutlatande'
            .'ns1:funktionstillstand'
            .findAll {it.'ns1:typAvFunktionstillstand'.text() == 'Aktivitet' }[0]
            .'arbetsformaga'.'ns1:motivering'.text()
        assert motivering != null
        def document = new JsonSlurper().parseText(json)
        document.observationer.each {observation ->
            if (observation.observationskod?.code == "302119000") {
                observation.prognoser = [[beskrivning: motivering]]
            }
        }
        JsonOutput.toJson(document)
    }
    
}
