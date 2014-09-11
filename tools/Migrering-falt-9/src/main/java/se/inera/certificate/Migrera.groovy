package se.inera.certificate

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder
import groovyx.gpars.GParsPool
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient

import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.dbcp2.BasicDataSource
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.AbstractHttpClient
import org.apache.http.params.HttpParams
import org.apache.http.util.EntityUtils;

/**
 * Migrering av de intyg som drabbats av fält9-problematiken
 */
class Migrera {

    static void main(String[] args) {
        println "--- Program start ---"
        println "- Starting Fält 9 conversion"
        
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
                
        println "- ${certificateIds.size()} candidates found"
        
        final AtomicInteger totalCount = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)
        def results
        def converted = 0
        def error = 0
        
        def converterUrl = config.webService.convert.URL
        
        def withPool = GParsPool.withPool(numberOfThreads) {
            
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
                    }
                }
                
                http.shutdown()
                
                if (updatedDocument) {
                    sql.execute('update CERTIFICATE set DOCUMENT = :document where ID = :id' , [document: updatedDocument.bytes, id : id])
                }
                
                sql.close()
                
                int current = totalCount.addAndGet(1)
                                
                if (current % 1000 == 0) {
                    println "- ${current} certificates processed, ${errorCount} errors"
                }
                result.toString()
            }
            results
        }
         
        long end = System.currentTimeMillis()
        
        println "- ${totalCount} certificates processed with ${errorCount} errors in ${end-start % 1000} seconds"
        
        if (results.size() > 0) {
            println " "
            for (err in results) {
                println err
            }
        }
        
    }
    
}
