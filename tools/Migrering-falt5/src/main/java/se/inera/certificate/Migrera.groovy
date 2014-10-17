package se.inera.certificate

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovyx.gpars.GParsPool

import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.dbcp2.BasicDataSource

class Migrera {

    static void main(String[] args) {
        int numberOfThreads = args.length > 0 ? args[0] : 10
        long start = System.currentTimeMillis()
        def props = new Properties()
        new File("dataSource.properties").withInputStream { stream ->
            props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        BasicDataSource dataSource =
            new BasicDataSource(driverClassName: config.dataSource.driver, url: config.dataSource.url,
                                username: config.dataSource.username, password: config.dataSource.password,
                                initialSize: numberOfThreads, maxTotal: numberOfThreads)
        def bootstrapSql = new Sql(dataSource)
        def allaIntyg = bootstrapSql.rows("select ID from CERTIFICATE")
        bootstrapSql.close()
        final AtomicInteger count = new AtomicInteger(0)
        final AtomicInteger countUpdated = new AtomicInteger(0)
        def output
        GParsPool.withPool(numberOfThreads) {
            output = allaIntyg.collectParallel {
                StringBuffer result = new StringBuffer() 
                def id = it.ID
                Sql sql = new Sql(dataSource)
                def row = sql.firstRow( 'select DOCUMENT from CERTIFICATE where ID = :id' , [id : id])
                def document = new JsonSlurper().parseText(new String(row.DOCUMENT, 'UTF-8'))
                // Check whether observations with the observation code 'd' contain a 'beskrivning' 
                // if not, delete the observation since it is incorrect.
                def observationer = document.observationer
                boolean jsonUpdated = false
                if (observationer != null) {
                    //count backwards in order to preserve index for following objects if one is removed
                    for (int i = observationer.size-1; i >= 0; i--) {
                        if (observationer[i].observationskategori != null) {
                            if(observationer[i].observationskategori.code == 'd' && !observationer[i].beskrivning) {
                                observationer.remove(observationer[i])
                                jsonUpdated = true
                            }
                        }
                    }
                }
                // If json is updated, convert back to byte array
                def updatedDocument = jsonUpdated ? new JsonBuilder(document).toString().getBytes('UTF-8') : null

                if (updatedDocument) {
                    sql.execute('update CERTIFICATE set DOCUMENT = :document where ID = :id' , [document: updatedDocument, id : id])
                    result << "$id updated - removed faulty observation"
                    countUpdated.addAndGet(1)
                } 
                sql.close()
                int current = count.addAndGet(1)
                if (current % 10000 == 0) {
                    println current
                }
                result.toString()
            }
        }
        long end = System.currentTimeMillis()
        output.each {line ->
            if (line) println line
        }
        println "$count certificates checked in ${end-start} milliseconds"
        println "$countUpdated certificates updated"
    }

}
