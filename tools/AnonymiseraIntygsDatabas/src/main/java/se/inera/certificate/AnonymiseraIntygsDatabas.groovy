package se.inera.certificate

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql

class AnonymiseraIntygsDatabas {

    static void main(String[] args) {
        def props = new Properties()
        new File("dataSource.properties").withInputStream {
          stream -> props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        AnonymiseraPersonId anonymiseraPersonId = new AnonymiseraPersonId()
        AnonymiseraHsaId anonymiseraHsaId = new AnonymiseraHsaId()
        AnonymiseraJson anonymiseraJson = new AnonymiseraJson(anonymiseraHsaId)
        AnonymiseraXml anonymiseraXml = new AnonymiseraXml(anonymiseraPersonId, anonymiseraHsaId)
        def sql = Sql.newInstance(config.datasource.url, config.dataSource.username, config.dataSource.password, config.dataSource.driver)
        int count = 1
        // Anonymisera alla befintliga intyg, och deras original-meddelanden
        sql.eachRow('select c.ID from CERTIFICATE c') {row ->
            print "${count++}: "
            def intyg = sql.firstRow( 'select DOCUMENT, CIVIC_REGISTRATION_NUMBER, SIGNING_DOCTOR_NAME from CERTIFICATE where ID = :id' , [id : row.ID])
            String jsonDoc = new String(intyg.DOCUMENT, 'UTF-8')
            String civicRegistrationNumber = anonymiseraPersonId.anonymisera(intyg.CIVIC_REGISTRATION_NUMBER)
            String signingDoctorName = AnonymizeString.anonymize(intyg.SIGNING_DOCTOR_NAME)
            String anonymiseradJson = anonymiseraJson.anonymiseraIntygsJson(jsonDoc, civicRegistrationNumber)
            sql.executeUpdate('update CERTIFICATE set DOCUMENT = :document, CIVIC_REGISTRATION_NUMBER = :civicRegistrationNumber, SIGNING_DOCTOR_NAME = :signingDoctorName where ID = :id',
                                  [document: anonymiseradJson.getBytes('UTF-8'), civicRegistrationNumber: civicRegistrationNumber, signingDoctorName: signingDoctorName, id: row.ID])
            def original = sql.firstRow( 'select DOCUMENT from ORIGINAL_CERTIFICATE where CERTIFICATE_ID = :id' , [id : row.ID])
            String xmlDoc = original?.DOCUMENT ? new String(original.DOCUMENT, 'UTF-8') : null
            String anonymiseradXml = xmlDoc ? anonymiseraXml.anonymiseraIntygsXml(xmlDoc, civicRegistrationNumber) : null
            if (anonymiseradXml) sql.executeUpdate('update ORIGINAL_CERTIFICATE set DOCUMENT = :document where CERTIFICATE_ID = :id',
                                  [document: anonymiseradXml.getBytes('UTF-8'), id : row.ID])
            println "${row.ID}"
        }
        // Anonymisera eventuella original-meddelanden som saknar motsvarande intyg
        sql.eachRow('select oc.ID from ORIGINAL_CERTIFICATE oc where oc.CERTIFICATE_ID is null') {row ->
            print "${count++}: "
            def original = sql.firstRow( 'select DOCUMENT from ORIGINAL_CERTIFICATE where ID = :id' , [id : row.ID])
            String xmlDoc = original?.DOCUMENT ? new String(original.DOCUMENT, 'UTF-8') : null
            String anonymiseradXml = xmlDoc ? anonymiseraXml.anonymiseraIntygsXml(xmlDoc) : null
            if (anonymiseradXml) sql.executeUpdate('update ORIGINAL_CERTIFICATE set DOCUMENT = :document where ID = :id',
                                  [document: anonymiseradXml.getBytes('UTF-8'), id : row.ID])
            println "${row.ID}"
        }
    }
}
    


