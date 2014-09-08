package se.inera.certificate

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
            def vardkontakter = intyg.vardkontakter
            vardkontakter.each {vardkontakt ->
                if (vardkontakt.vardkontaktstid.start && vardkontakt.vardkontaktstid.end) {
                    vardkontakt.vardkontaktstid.from = vardkontakt.vardkontaktstid.start
                    vardkontakt.vardkontaktstid.remove('start')
                    vardkontakt.vardkontaktstid.tom = vardkontakt.vardkontaktstid.end
                    vardkontakt.vardkontaktstid.remove('end')
                } else {
                    println "start/end attribut saknas för intyg ${id}:"
                    println JsonOutput.toJson(vardkontakt)
                }
            }
            def document = JsonOutput.toJson(intyg).bytes
            sqlUpdate.execute('update CERTIFICATE set DOCUMENT = :document where ID = :id' , [document: document, id : id])
            if (++count % 1000 == 0) {
                println count
            }
        }
        println "$count intyg uppdaterade"
    }

}
