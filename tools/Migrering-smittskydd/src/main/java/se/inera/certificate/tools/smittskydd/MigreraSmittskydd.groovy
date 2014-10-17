package se.inera.certificate.tools.smittskydd

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
 * Migrering av de intyg som drabbats av smittskydd-problematiken
 */
class MigreraSmittskydd {

    static void main(String[] args) {

        println "- Starting smittskydd - Certificate migration"
        
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
                                username: config.dataSource.username, password: config.dataSource.password)
        def bootstrapSql = new Sql(dataSource)
        def certificateIds = bootstrapSql.rows("select ID from ORIGINAL_CERTIFICATE where CERTIFICATE_ID is NULL")
        bootstrapSql.close()
                
        println "- ${certificateIds.size()} candidates found"
        
        int totalCount = 0
        int errorCount = 0
        
        def converterUrl = config.webService.convert.URL
        
        certificateIds.each {
            def id = it.ID
            Sql sql = new Sql(dataSource)
            def row = sql.firstRow("select DOCUMENT from ORIGINAL_CERTIFICATE where ID = :id", [id: id])
            def originalDocument = new String(row.DOCUMENT, 'UTF-8')
            def slurper = new XmlSlurper()
            def intyg = slurper.parseText(originalDocument)
            intyg.declareNamespace(ns1: 'urn:riv:insuranceprocess:healthreporting:mu7263:3',
                                   ns2: 'urn:riv:insuranceprocess:healthreporting:2',
                                   ns3: 'urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3')
            def intygsId = intyg.'ns3:lakarutlatande'.'ns1:lakarutlatande-id'.text()
            sql.execute('update ORIGINAL_CERTIFICATE set CERTIFICATE_ID = :certificate_id where ID = :id', [certificate_id: intygsId, id: id])
                            
            def updatedDocument = null
            
            def http = new HTTPBuilder(converterUrl);
            
            try {
                http.request(POST, XML) {
                    headers.'Accept' = 'application/json'
                    headers.'ContentEncoding' = 'UTF-8'
                    body = originalDocument
                    response.success = { resp ->
                        updatedDocument = EntityUtils.toString(resp.entity, 'UTF-8')
                    }
                    response.failure = { resp ->
                        def msg = EntityUtils.toString(resp.entity, 'UTF-8')
                        println "${id};${resp.status};${msg}"
                        errorCount++
                    }
                }
            } catch (Throwable t) {
                println "${id};${t}"
                errorCount++
            } finally {
                http.shutdown()
            }            
            
            if (updatedDocument) {
                sql.execute('update CERTIFICATE set DOCUMENT = :document where ID = :id' , [document: updatedDocument.getBytes('UTF-8'), id : intygsId])
            }
            
            sql.close()
            
            totalCount++
                            
            if (totalCount % 10 == 0) {
                println "- ${totalCount} certificates processed in ${(int)((System.currentTimeMillis()-start) / 1000)} seconds, ${errorCount} errors"
            }
        }
         
        long end = System.currentTimeMillis()
        
        println "- Done! ${totalCount} certificates processed with ${errorCount} errors in ${(int)((end-start) / 1000)} seconds"
        
    }
    
}
