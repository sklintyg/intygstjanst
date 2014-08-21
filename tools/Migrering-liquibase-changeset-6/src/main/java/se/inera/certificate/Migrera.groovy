package se.inera.certificate

import groovy.sql.Sql
import groovy.json.JsonSlurper

class Migrera {

    static void main(String[] args) {
        def props = new Properties()
        new File("dataSource.properties").withInputStream { stream ->
            props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        Sql sqlReadonly = Sql.newInstance(config.datasource.url, config.dataSource.username, config.dataSource.password, config.dataSource.driver)
        sqlReadonly.resultSetConcurrency = java.sql.ResultSet.CONCUR_READ_ONLY
        sqlReadonly.withStatement{stmt -> stmt.fetchSize = Integer.MIN_VALUE }
        Sql sqlUpdate = Sql.newInstance(config.datasource.url, config.dataSource.username, config.dataSource.password, config.dataSource.driver)
        int count = 0
        sqlReadonly.eachRow("select ID, DOCUMENT from CERTIFICATE where CARE_UNIT_ID = 'x'") { row ->
            def id = row.ID
            def intyg = new JsonSlurper().parseText(new String(row.DOCUMENT, 'UTF-8'))
            def enhet = intyg.skapadAv.vardenhet.id.extension
            if (enhet) {
                sqlUpdate.execute('update CERTIFICATE set CARE_UNIT_ID = :enhet where ID = :id' , [enhet: enhet, id : id])
            } else {
                println "$id saknar giltig enhet - ej uppdaterad"
            }
            if (++count % 1000 == 0) {
                println count
            }
        }
        println "$count intyg uppdaterade"
    }

}
