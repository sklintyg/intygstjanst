package se.inera.certificate.tools

import groovy.sql.Sql
import groovyx.gpars.GParsPool

import java.util.concurrent.atomic.AtomicInteger

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller

import org.apache.commons.dbcp2.BasicDataSource

import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper
import se.inera.intyg.intygstyper.fk7263.model.converter.TransportToInternal
import se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande
import se.inera.ifv.insuranceprocess.healthreporting.mu7263.v3.LakarutlatandeType
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType

/**
 * Migrering av intyg
 */
class Korrigera {

    static void main(String[] args) {

        println "- Starting Certificate correction for INTYG-1413"
        
        int numberOfThreads = args.length > 0 ? Integer.parseInt(args[0]) : 10
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
        def certificateIds = bootstrapSql.rows("select CERTIFICATE_ID from ORIGINAL_CERTIFICATE oc where oc.DOCUMENT not like '%Nuvarande_arbete%' and CERTIFICATE_ID in (select ID from CERTIFICATE c where c.DOCUMENT like '%nuvarandeArbete\":true%');")
        bootstrapSql.close()
          
        long end = System.currentTimeMillis()
        
        println "- ${certificateIds.size()} candidates found in ${(int)((end-start) / 1000)} seconds"
        
        start = System.currentTimeMillis()
        
        final AtomicInteger totalCount = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)
        
        def results
        
        JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class)
        CustomObjectMapper objectMapper = new CustomObjectMapper()
        
        GParsPool.withPool(numberOfThreads) {
            
            results = certificateIds.collectParallel {
                StringBuffer result = new StringBuffer()
                def id = it.CERTIFICATE_ID
                Sql sql = new Sql(dataSource)
                try {
                    def row = sql.firstRow( 'select DOCUMENT from ORIGINAL_CERTIFICATE where CERTIFICATE_ID = :id' , [id : id])
                    def originalDocument = new String(row.DOCUMENT, 'UTF-8')
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    LakarutlatandeType lakarutlatandeType =
                        unmarshaller.unmarshal(new StringReader(originalDocument)).value.lakarutlatande
                    Utlatande utlatande = TransportToInternal.convert(lakarutlatandeType)
                    StringWriter writer = new StringWriter();
                    objectMapper.writeValue(writer, utlatande);
                    def document = writer.toString()

                    sql.execute('update CERTIFICATE set DOCUMENT = :document where ID = :id', [document: document.getBytes('UTF-8'), id : id])
                } catch (Exception e) {
                    result << "${id};${e.message}"
                    errorCount.incrementAndGet()
                }
                sql.close()
                
                int current = totalCount.incrementAndGet()
                                
                if (current % 1000 == 0) {
                    println "- ${current} certificates processed in ${(int)((System.currentTimeMillis()-start) / 1000)} seconds, ${errorCount} errors"
                }
                result.toString()
            }
        }
         
        end = System.currentTimeMillis()
        
        println "- Done! ${totalCount} certificates processed with ${errorCount} errors in ${(int)((end-start) / 1000)} seconds"
        
        println " "
        results.each { line ->
            if (line) println line
        }
        
    }
    
}
