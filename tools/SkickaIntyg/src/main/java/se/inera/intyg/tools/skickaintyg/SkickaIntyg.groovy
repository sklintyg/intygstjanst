package se.inera.certificate.tools

import groovy.sql.Sql

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller

import org.apache.commons.dbcp2.BasicDataSource
import org.apache.cxf.frontend.ClientProxyFactoryBean
import org.apache.cxf.jaxws.JaxWsClientFactoryBean
import org.w3.wsaddressing10.AttributedURIType

import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum
import se.inera.webcert.medcertqa.v1.LakarutlatandeEnkelType
import se.inera.webcert.medcertqa.v1.VardAdresseringsType

/**
 * SkickaIntyg:
 * 
 * Rutin för att skicka intyg som är registrerade i Intygstjänsten till FK.
 * 
 * intygs-id:n för de intyg som skall skickas anges som kommando-rads-parametrar.
 * 
 * Konfiguration:
 * I samma katalog som denna applikation körs i förvändas två properties-filer finnas:
 * 
 *   dataSource.properties, som definierar datakälla för att läsa upp intygsinformation
 *     dataSource.url=..
 *     dataSource.driver=..
 *     dataSource.username=..
 *     dataSource.password=..
 *
 *   webService.properties, som definierar URLen till SendMedicalCertificate-tjänsten
 *     webService.send.URL=..
 */
class SkickaIntyg {

    static def createClient(def responderInterface, String url) {
        ClientProxyFactoryBean factory = new ClientProxyFactoryBean(new JaxWsClientFactoryBean());
        factory.setServiceClass( responderInterface );
        factory.setAddress(url);
        def responder = factory.create();
    }


    static void main(String[] args) {
        
        println "- Startar sändning av intyg"
        
        long start = System.currentTimeMillis()
        def props = new Properties()
        new File("dataSource.properties").withInputStream { stream ->
            props.load(stream)
        }
        new File("webService.properties").withInputStream { stream ->
            props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        
        SendMedicalCertificateResponderInterface sendResponder =
            createClient(SendMedicalCertificateResponderInterface.class, config.webService.send.URL)
        AttributedURIType logicalAddress = new AttributedURIType()
        logicalAddress.value = "notUsed"
  
        BasicDataSource dataSource =
            new BasicDataSource(driverClassName: config.dataSource.driver, url: config.dataSource.url,
                                username: config.dataSource.username, password: config.dataSource.password,
                                initialSize: 1, maxTotal: 1)
        def bootstrapSql = new Sql(dataSource)
        
        int totalCount = 0
        int warningCount = 0
        int errorCount = 0
        
        JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class)
        Sql sql = new Sql(dataSource)
        
        args.each { id ->
            try {
                def row = sql.firstRow( 'select DOCUMENT from ORIGINAL_CERTIFICATE where CERTIFICATE_ID = :id' , [id : id])
                if (row) {
                    def originalDocument = new String(row.DOCUMENT, 'UTF-8')
                    // Use the following for H2, which returns the document as a blob instead of a byte array
                    // def originalDocument = new String(row.DOCUMENT.getBytes(0L, (int) row.DOCUMENT.length()), 'UTF-8')
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    RegisterMedicalCertificateType registerRequest =
                        unmarshaller.unmarshal(new StringReader(originalDocument)).value
    
                    SendMedicalCertificateRequestType sendRequestType = new SendMedicalCertificateRequestType()
                    SendType sendType = new SendType()
                    sendRequestType.setSend(sendType)
                    sendType.vardReferensId = 1
                    sendType.avsantTidpunkt = registerRequest.lakarutlatande.skickatDatum
                    sendType.adressVard = new VardAdresseringsType()
                    sendType.adressVard.hosPersonal = registerRequest.lakarutlatande.skapadAvHosPersonal
                    sendType.lakarutlatande = new LakarutlatandeEnkelType()
                    sendType.lakarutlatande.lakarutlatandeId = registerRequest.lakarutlatande.lakarutlatandeId
                    sendType.lakarutlatande.signeringsTidpunkt = registerRequest.lakarutlatande.signeringsdatum
                    sendType.lakarutlatande.patient = registerRequest.lakarutlatande.patient
            
                    SendMedicalCertificateResponseType sendResponse = sendResponder.sendMedicalCertificate(logicalAddress, sendRequestType)
                    
                    if (sendResponse.result.resultCode != ResultCodeEnum.OK) {
                        if (sendResponse.result.resultCode == ResultCodeEnum.INFO) {
                            println "sändning av intyg ${id} gav varning: ${sendResponse.result.infoText}"
                            warningCount++
                        } else {
                            println "sändning av intyg ${id} misslyckades: [${sendResponse.result.errorId.toString()}]-${sendResponse.result.errorText}"
                            errorCount++
                        }
                    } else {
                        println "sändning av intyg ${id} lyckades"
                    }
                } else {
                    println "intyg med id ${id} saknas"
                    errorCount++
                }
            } catch (Exception e) {
                println "sändning av intyg ${id} misslyckades: ${e.message}"
                errorCount++
            }
            totalCount++
        }
         
        sql.close()
        long end = System.currentTimeMillis()
        
        println "- Klart! ${totalCount} intyg processad med ${warningCount} varningar och ${errorCount} fel på ${(int)((end-start) / 1000)} sekunder"
        
    }

}
