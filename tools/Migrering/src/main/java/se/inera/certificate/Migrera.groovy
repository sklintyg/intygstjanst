package se.inera.certificate

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.Sql

class Migrera {

    static void main(String[] args) {
        def props = new Properties()
        new File("dataSource.properties").withInputStream { stream ->
            props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        Sql sqlReadonly = Sql.newInstance(config.dataSource.url, config.dataSource.username, config.dataSource.password, config.dataSource.driver)
        sqlReadonly.resultSetConcurrency = java.sql.ResultSet.CONCUR_READ_ONLY
        sqlReadonly.withStatement{stmt -> stmt.fetchSize = Integer.MIN_VALUE }
        Sql sqlUpdate = Sql.newInstance(config.dataSource.url, config.dataSource.username, config.dataSource.password, config.dataSource.driver)
        int count = 0
        sqlReadonly.eachRow("select ID, DOCUMENT from CERTIFICATE") { row ->
            def id = row.ID
            def intyg = new JsonSlurper().parseText(new String(row.DOCUMENT, 'UTF-8'))
            // Rename 'start' and 'end' attributes of vardkontaktstid to 'from' and 'tom'
            // to make them consistent with other date intervals
            def vardkontakter = intyg.vardkontakter
            vardkontakter.each {vardkontakt ->
                if (vardkontakt.vardkontaktstid.start && vardkontakt.vardkontaktstid.end) {
                    vardkontakt.vardkontaktstid.from = vardkontakt.vardkontaktstid.start
                    vardkontakt.vardkontaktstid.remove('start')
                    vardkontakt.vardkontaktstid.tom = vardkontakt.vardkontaktstid.end
                    vardkontakt.vardkontaktstid.remove('end')
                } else {
                    println "start/end missing for certificate ${id}:"
                    println JsonOutput.toJson(vardkontakt)
                }
            }
            def document = new JsonBuilder(intyg).toString().getBytes('UTF-8')
            
            // Extract care unit id and set corresponding metadata column
            def enhet = intyg.skapadAv.vardenhet.id.extension
            if (enhet) {
                sqlUpdate.execute('update CERTIFICATE set DOCUMENT = :document, CARE_UNIT_ID = :enhet where ID = :id' , [document: document, enhet: enhet, id : id])
            } else {
                sqlUpdate.execute('update CERTIFICATE set DOCUMENT = :document where ID = :id' , [document: document, id : id])
                println "$id lacks care unit id - not updated"
            }
            if (++count % 1000 == 0) {
                println count
            }
        }
        println "$count certificates uppdated"
    }

}
