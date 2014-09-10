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
        def allaIntyg = bootstrapSql.rows("""
            select oc.CERTIFICATE_ID from ORIGINAL_CERTIFICATE oc
            where oc.DOCUMENT like '%motivering>%' 
            and oc.DOCUMENT not like '%prognosangivelse>%'
            and oc.CERTIFICATE_ID in (
                select cs.CERTIFICATE_ID from CERTIFICATE_STATE cs
                where cs.STATE = 'RECEIVED'
                and cs.TIMESTAMP < '2014-06-02 10:06:00'
            )""")
        bootstrapSql.close()
        
        final AtomicInteger count = new AtomicInteger(0)
        def output
        GParsPool.withPool(numberOfThreads) {
            output = allaIntyg.collectParallel {
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
        output.each {line ->
            if (line) println line
        }
        long end = System.currentTimeMillis()
        println "$count certificates migrated in ${end-start} milliseconds"

        Sql sql = new Sql(dataSource)
        int sent = 0
        allaIntyg.each {
            def id = it.CERTIFICATE_ID
            def sentState = sql.rows( "select cs1.CERTIFICATE_ID from CERTIFICATE_STATE cs1 " +
                                      "left join CERTIFICATE_STATE cs2 on cs1.CERTIFICATE_ID = cs2.CERTIFICATE_ID " +
                                      "where cs1.CERTIFICATE_ID = :id " +
                                      "and cs1.STATE = 'RECEIVED' " +
                                      "and cs2.STATE = 'SENT' "+
                                      "and TIMESTAMPDIFF(SECOND, cs1.TIMESTAMP, cs2.TIMESTAMP) > 1 " +
                                      "and cs1.CERTIFICATE_ID not in (" +
                                      "  select cs3.CERTIFICATE_ID from CERTIFICATE_STATE cs3 " +
                                      "  where cs3.STATE = 'CANCELLED')", [id : id])
            if (sentState) {
                def row = sql.firstRow( 'select DOCUMENT from ORIGINAL_CERTIFICATE where CERTIFICATE_ID = :id' , [id : id])
                println "$id should be sent"
                sent++
                Thread.sleep(1)
            }
        }
        sql.close()
        println "$sent certificates sent to FK"
    }

}
