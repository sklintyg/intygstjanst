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
        def output
        GParsPool.withPool(numberOfThreads) {
            output = allaIntyg.collectParallel {
                StringBuffer result = new StringBuffer() 
                def id = it.ID
                Sql sql = new Sql(dataSource)
                def row = sql.firstRow( 'select DOCUMENT from CERTIFICATE where ID = :id' , [id : id])
                def document = new JsonSlurper().parseText(new String(row.DOCUMENT, 'UTF-8'))
                // Rename 'start' and 'end' attributes of vardkontaktstid to 'from' and 'tom'
                // to make them consistent with other date intervals
                def vardkontakter = document.vardkontakter
                boolean jsonUpdated = false
                vardkontakter.each {vardkontakt ->
                    if (vardkontakt.vardkontaktstid.start && vardkontakt.vardkontaktstid.end) {
                        vardkontakt.vardkontaktstid.from = vardkontakt.vardkontaktstid.start
                        vardkontakt.vardkontaktstid.remove('start')
                        vardkontakt.vardkontaktstid.tom = vardkontakt.vardkontaktstid.end
                        vardkontakt.vardkontaktstid.remove('end')
                        jsonUpdated = true
                    } else {
                        result << "start/end missing in vardkontakt for certificate ${id}:"
                        result <<  JsonOutput.toJson(vardkontakt)
                    }
                }
                // If json is updated, convert back to byte array
                def updatedDocument = jsonUpdated ? new JsonBuilder(document).toString().getBytes('UTF-8') : null
                
                // Extract care unit id and set corresponding metadata column
                def enhet = document.skapadAv.vardenhet.id.extension
                if (updatedDocument && enhet) {
                    sql.execute('update CERTIFICATE set DOCUMENT = :document, CARE_UNIT_ID = :enhet where ID = :id' , [document: updatedDocument, enhet: enhet, id : id])
                } else if (updatedDocument) {
                    sql.execute('update CERTIFICATE set DOCUMENT = :document where ID = :id' , [document: updatedDocument, id : id])
                    result << "$id lacks care unit id - care unit id not updated"
                } else if (enhet) {
                    sql.execute('update CERTIFICATE set CARE_UNIT_ID = :enhet where ID = :id' , [enhet: enhet, id : id])
                } else {
                    result << "$id lacks care unit id and start/end attributes - not updated"
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
        println "$count certificates uppdated in ${end-start} milliseconds"
    }

}
