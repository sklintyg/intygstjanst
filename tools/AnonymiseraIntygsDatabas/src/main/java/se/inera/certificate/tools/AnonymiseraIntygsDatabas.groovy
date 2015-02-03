package se.inera.certificate.tools

import groovy.sql.Sql
import groovyx.gpars.GParsPool

import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.dbcp2.BasicDataSource

import se.inera.certificate.tools.anonymisering.AnonymiseraDatum;
import se.inera.certificate.tools.anonymisering.AnonymiseraHsaId;
import se.inera.certificate.tools.anonymisering.AnonymiseraJson;
import se.inera.certificate.tools.anonymisering.AnonymiseraPersonId;
import se.inera.certificate.tools.anonymisering.AnonymiseraXml;
import se.inera.certificate.tools.anonymisering.AnonymizeString;

class AnonymiseraIntygsDatabas {

    static void main(String[] args) {
        println "Starting anonymization"
        
        int numberOfThreads = args.length > 0 ? Integer.parseInt(args[0]) : 5
        long start = System.currentTimeMillis()
        AnonymiseraPersonId anonymiseraPersonId = new AnonymiseraPersonId()
        AnonymiseraHsaId anonymiseraHsaId = new AnonymiseraHsaId()
        AnonymiseraDatum anonymiseraDatum = new AnonymiseraDatum()
        AnonymiseraJson anonymiseraJson = new AnonymiseraJson(anonymiseraHsaId, anonymiseraDatum)
        AnonymiseraXml anonymiseraXml = new AnonymiseraXml(anonymiseraPersonId, anonymiseraHsaId, anonymiseraDatum)
        def props = new Properties()
        new File("dataSource.properties").withInputStream {
          stream -> props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        BasicDataSource dataSource =
            new BasicDataSource(driverClassName: config.dataSource.driver, url: config.dataSource.url,
                                username: config.dataSource.username, password: config.dataSource.password,
                                initialSize: numberOfThreads, maxTotal: numberOfThreads)
        def bootstrapSql = new Sql(dataSource)
        def certificateIds = bootstrapSql.rows("select ID from CERTIFICATE")
        bootstrapSql.close()
        println "${certificateIds.size()} certificates found to anonymize"
        final AtomicInteger count = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)
        def output
        GParsPool.withPool(numberOfThreads) {
            output = certificateIds.collectParallel {
                StringBuffer result = new StringBuffer() 
                def id = it.ID
                Sql sql = new Sql(dataSource)
                try {
                    // Anonymisera alla befintliga intyg, och deras original-meddelanden
                    def intyg = sql.firstRow( 'select DOCUMENT, CIVIC_REGISTRATION_NUMBER, SIGNING_DOCTOR_NAME from CERTIFICATE where ID = :id' , [id : id])
                    String jsonDoc = new String(intyg.DOCUMENT, 'UTF-8')
                    String civicRegistrationNumber = anonymiseraPersonId.anonymisera(intyg.CIVIC_REGISTRATION_NUMBER)
                    String signingDoctorName = AnonymizeString.anonymize(intyg.SIGNING_DOCTOR_NAME)
                    String anonymiseradJson = anonymiseraJson.anonymiseraIntygsJson(jsonDoc, civicRegistrationNumber)
                    sql.executeUpdate('update CERTIFICATE set DOCUMENT = :document, CIVIC_REGISTRATION_NUMBER = :civicRegistrationNumber, SIGNING_DOCTOR_NAME = :signingDoctorName where ID = :id',
                                          [document: anonymiseradJson.getBytes('UTF-8'), civicRegistrationNumber: civicRegistrationNumber, signingDoctorName: signingDoctorName, id: id])
                    def original = sql.firstRow( 'select DOCUMENT from ORIGINAL_CERTIFICATE where CERTIFICATE_ID = :id' , [id : id])
                    String xmlDoc = original?.DOCUMENT ? new String(original.DOCUMENT, 'UTF-8') : null
                    String anonymiseradXml = xmlDoc ? anonymiseraXml.anonymiseraIntygsXml(xmlDoc, civicRegistrationNumber) : null
                    if (anonymiseradXml) sql.executeUpdate('update ORIGINAL_CERTIFICATE set DOCUMENT = :document where CERTIFICATE_ID = :id',
                                          [document: anonymiseradXml.getBytes('UTF-8'), id : id])
                    int current = count.addAndGet(1)
                    if (current % 10000 == 0) {
                        println "${current} certificates anonymized in ${(int)((System.currentTimeMillis()-start) / 1000)} seconds"
                    }
                } catch (Throwable t) {
                    result << "Anonymizing ${id} failed: ${t}"
                    errorCount.incrementAndGet()
                } finally {
                    sql.close()
                }
                result.toString()
            }
        }
        // Anonymisera eventuella original-meddelanden som saknar länk till motsvarande intyg
        Sql sql = new Sql(dataSource)
        sql.eachRow('select oc.ID from ORIGINAL_CERTIFICATE oc where oc.CERTIFICATE_ID is null') {row ->
            // print "${count++}: "
            def original = sql.firstRow( 'select DOCUMENT from ORIGINAL_CERTIFICATE where ID = :id' , [id : row.ID])
            String xmlDoc = original?.DOCUMENT ? new String(original.DOCUMENT, 'UTF-8') : null
            String anonymiseradXml = xmlDoc ? anonymiseraXml.anonymiseraIntygsXml(xmlDoc) : null
            if (anonymiseradXml) sql.executeUpdate('update ORIGINAL_CERTIFICATE set DOCUMENT = :document where ID = :id',
                                  [document: anonymiseradXml.getBytes('UTF-8'), id : row.ID])
            int current = count.addAndGet(1)
            if (current % 10000 == 0) {
                println "${current} certificates anonymized in ${(int)((System.currentTimeMillis()-start) / 1000)} seconds"
            }
        }
        sql.close()
        long end = System.currentTimeMillis()
        output.each {line ->
            if (line) println line
        }
        println "Done! ${count} certificates anonymized with ${errorCount} errors in ${(int)((end-start) / 1000)} seconds"
    }
}
    


