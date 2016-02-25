/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.tools.sjukfall

import groovy.sql.Sql
import groovyx.gpars.GParsPool

import org.joda.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.dbcp2.BasicDataSource

import se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande

/**
 * Skapa Sjukfall av intyg.
 *
 * @author eriklupander
 */
class SkapaSjukfall {

    static void main(String[] args) {

        println "- Starting Intyg -> Sjukfall creation"
        
        int numberOfThreads = args.length > 0 ? Integer.parseInt(args[0]) : 5
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
        def certificateIds = bootstrapSql.rows("select c.ID from CERTIFICATE c WHERE c.CERTIFICATE_TYPE = 'fk7263'")
        bootstrapSql.close()
                
        println "- ${certificateIds.size()} candidates for being processed into sjukfall found"
        
        final AtomicInteger totalCount = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)
        final AtomicInteger recoverCount = new AtomicInteger(0)

        def results

        // CustomObjectMapper objectMapper = new CustomObjectMapper()
        se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper objectMapper = new  se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper()

        GParsPool.withPool(numberOfThreads) {

            results = certificateIds.collectParallel {
                StringBuffer result = new StringBuffer()
                def id = it.ID
                Sql sql = new Sql(dataSource)
                try {
                    def row = sql.firstRow( 'select DOCUMENT from CERTIFICATE where ID = :id AND DELETED = :deleted'
                            , [id : id, deleted : false])
                    if (row == null || row.DOCUMENT == null) {
                        println "Intyg ${id} has no DOCUMENT, skipping."
                        throw new Exception("Intyg ${id} has no DOCUMENT, skipping.")
                    }
                    def originalDocument = new String(row.DOCUMENT, 'UTF-8')
                    Utlatande utlatande = objectMapper.readValue(originalDocument, Utlatande.class)

                    String diagnosKod = utlatande.diagnosKod
                    if (diagnosKod == null) {
                        println "Intyg ${id} has no diagnosKod, skipping."
                        throw new Exception("Intyg ${id} has no diagnosKod, skipping.")
                    }

                    String personnummer = utlatande.grundData.patient.personId.personnummer
                    String firstName = utlatande.grundData.patient.fornamn
                    String lastName = utlatande.grundData.patient.efternamn
                    String careUnitId = utlatande.grundData.skapadAv.vardenhet.enhetsid
                    String careUnitName = utlatande.grundData.skapadAv.vardenhet.enhetsnamn
                    String care_giver_id = utlatande.grundData.skapadAv.vardenhet.vardgivare.vardgivarid
                    String doctorId = utlatande.grundData.skapadAv.personId
                    String doctorName = utlatande.grundData.skapadAv.fullstandigtNamn

                    Boolean deleted = false
                    LocalDateTime signingDateTime = utlatande.grundData.signeringsdatum
                    java.sql.Date sqlSigningDateTime = new java.sql.Date(signingDateTime.toDate().getTime())


                    // Insert base sjukfall cert data
                    sql.execute("INSERT INTO SJUKFALL_CERT "
                            + "(ID,CERTIFICATE_TYPE,CIVIC_REGISTRATION_NUMBER,PATIENT_FIRST_NAME,PATIENT_LAST_NAME,CARE_UNIT_ID,CARE_UNIT_NAME,CARE_GIVER_ID,SIGNING_DOCTOR_ID,SIGNING_DOCTOR_NAME,DIAGNOSE_CODE,DELETED,SIGNING_DATETIME)"
                            + "VALUES (:id,:type,:personnummer,:firstName,:lastName,:careUnitId,:careUnitName,:careGiverId,:doctorId,:doctorName,:diagnosKod,:deleted,:signingDateTime)"
                            , [id: id, type : utlatande.typ, personnummer: personnummer, firstName : firstName, lastName : lastName ,
                               careUnitId : careUnitId, careUnitName : careUnitName, careGiverId : care_giver_id, doctorId : doctorId,
                               doctorName : doctorName, diagnosKod : diagnosKod, deleted : deleted, signingDateTime : sqlSigningDateTime
                            ])

                    //sql.commit()

                    // Insert one item per nedsattning
                    String insertSql = "INSERT INTO SJUKFALL_CERT_WORK_CAPACITY (CERTIFICATE_ID,CAPACITY_PERCENTAGE,FROM_DATE,TO_DATE) VALUES(:id,:nedsattningProcent,:fromDate,:toDate)";
                    if (utlatande.nedsattMed25 != null) {
                        sql.execute(insertSql, [id:id,
                                                nedsattningProcent: 25,
                                                fromDate: utlatande.nedsattMed25.fromAsLocalDate().toString("yyyy-MM-dd"),
                                                toDate: utlatande.nedsattMed25.tomAsLocalDate().toString("yyyy-MM-dd")])
                    }
                    if (utlatande.nedsattMed50 != null) {
                        sql.execute(insertSql, [id:id,
                                                nedsattningProcent: 50,
                                                fromDate: utlatande.nedsattMed50.fromAsLocalDate().toString("yyyy-MM-dd"),
                                                toDate: utlatande.nedsattMed50.tomAsLocalDate().toString("yyyy-MM-dd")])
                    }
                    if (utlatande.nedsattMed75 != null) {
                        sql.execute(insertSql, [id:id,
                                                nedsattningProcent: 75,
                                                fromDate: utlatande.nedsattMed75.fromAsLocalDate().toString("yyyy-MM-dd"),
                                                toDate: utlatande.nedsattMed75.tomAsLocalDate().toString("yyyy-MM-dd")])
                    }
                    if (utlatande.nedsattMed100 != null) {
                        sql.execute(insertSql, [id:id,
                                                nedsattningProcent: 100,
                                                fromDate: utlatande.nedsattMed100.fromAsLocalDate().toString("yyyy-MM-dd"),
                                                toDate: utlatande.nedsattMed100.tomAsLocalDate().toString("yyyy-MM-dd")])
                    }

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
         
        long end = System.currentTimeMillis()
        
        println "- Done! ${totalCount} certificates processed into sjukfall with ${errorCount} errors in ${(int)((end-start) / 1000)} seconds"
        
        if (results.size() > 0) {
            println " "
            println "id;message"
            results.each { line ->
                if (line) println line
            }
        }
        
    }
    
}
