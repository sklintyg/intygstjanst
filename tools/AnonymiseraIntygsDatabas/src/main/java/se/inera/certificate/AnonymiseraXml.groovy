package se.inera.certificate

import groovy.xml.StreamingMarkupBuilder

class AnonymiseraXml {
    
    AnonymiseraPersonId anonymiseraPersonId;
    AnonymiseraHsaId anonymiseraHsaId;
    
    AnonymiseraXml(AnonymiseraPersonId anonymiseraPersonId, AnonymiseraHsaId anonymiseraHsaId) {
        this.anonymiseraPersonId = anonymiseraPersonId
        this.anonymiseraHsaId = anonymiseraHsaId
    }
    
    String anonymiseraIntygsXml(String s) {
        def intyg = new XmlSlurper().parseText(s)
        intyg.declareNamespace(ns1: 'urn:riv:insuranceprocess:healthreporting:mu7263:3',
                               ns2: 'urn:riv:insuranceprocess:healthreporting:2',
                               ns3: 'urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3')
        anonymizeXml(intyg)
        def outputBuilder = new StreamingMarkupBuilder()
        outputBuilder.encoding = 'UTF-8'
        return (s.startsWith('<?xml') ? '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' : "") + outputBuilder.bind{  mkp.yield intyg }
    }
    
    private void anonymizeXml(def intyg) {
        String personId = intyg.'ns3:lakarutlatande'.'ns1:patient'.'ns2:person-id'.@extension
        intyg.'ns3:lakarutlatande'.'ns1:patient'.'ns2:person-id'.@extension = anonymiseraPersonId.anonymisera(personId)
        anonymizeNode intyg.'ns3:lakarutlatande'.'ns1:patient'.'ns2:fullstandigtNamn'
        String personalId = intyg.'ns3:lakarutlatande'.'ns1:skapadAvHosPersonal'.'ns2:personal-id'.@extension
        intyg.'ns3:lakarutlatande'.'ns1:skapadAvHosPersonal'.'ns2:personal-id'.@extension = anonymiseraHsaId.anonymisera(personalId)
        anonymizeNode intyg.'ns3:lakarutlatande'.'ns1:skapadAvHosPersonal'.'ns2:fullstandigtNamn'
        anonymizeNode intyg.'ns3:lakarutlatande'.'ns1:skapadAvHosPersonal'.'ns2:forskrivarkod'
        anonymizeNode intyg.'ns3:lakarutlatande'?.'ns1:kommentar'
        intyg.'ns3:lakarutlatande'?.'ns1:aktivitet'?.each {
            anonymizeNode it.'ns1:beskrivning'
        }
        anonymizeNode intyg.'ns3:lakarutlatande'?.'ns1:bedomtTillstand'?.'ns1:beskrivning'
        anonymizeNode intyg.'ns3:lakarutlatande'?.'ns1:medicinsktTillstand'?.'ns1:beskrivning'
        intyg.'ns3:lakarutlatande'?.'ns1:funktionstillstand'?.each {
            anonymizeNode it.'ns1:beskrivning'
            anonymizeNode it.'ns1:arbetsformaga'?.'ns1:motivering'
            anonymizeNode it.'ns1:arbetsformaga'?.'ns1:arbetsuppgift'?.'ns1:typAvArbetsuppgift'
        }
    }
    
    private void anonymizeNode(def node) {
        node?.replaceBody AnonymizeString.anonymize(node.toString())
    }
}
