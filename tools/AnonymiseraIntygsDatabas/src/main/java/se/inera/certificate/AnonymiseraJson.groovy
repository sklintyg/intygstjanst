package se.inera.certificate

import groovy.json.*

class AnonymiseraJson {
    
    AnonymiseraHsaId anonymiseraHsaId;
    
    static {
        HashMap.metaClass.anonymize = {key->
            if (delegate[key]) {
                def value = delegate[key]
                if (value instanceof List) {
                    delegate[key] = value.collect {AnonymizeString.anonymize(it)}
                } else {
                    delegate[key] = AnonymizeString.anonymize(value)
                }
            }
        }
    }

    AnonymiseraJson(AnonymiseraHsaId anonymiseraHsaId) {
        this.anonymiseraHsaId = anonymiseraHsaId
    }
    
    String anonymiseraIntygsJson(String s, String personId) {
        def intyg = new JsonSlurper().parseText(s)
        anonymizeJson(intyg, personId)
        JsonBuilder builder = new JsonBuilder( intyg )
        return builder.toString()
    }
    
    void anonymizeJson(def intyg, String personId) {
        intyg.patient.id.extension = personId
        intyg.patient.anonymize('fornamn')
        intyg.patient.anonymize('efternamn')
        intyg.patient.anonymize('fullstandigtNamn')
        intyg.skapadAv.id.extension = anonymiseraHsaId.anonymisera(intyg.skapadAv.id.extension)
        intyg.skapadAv.anonymize('namn')
        intyg.skapadAv.anonymize('forskrivarkod')
        intyg.patient?.arbetsuppgifter?.each { it.anonymize('typAvArbetsuppgift') }
        intyg.anonymize('kommentarer')
        intyg.aktiviteter?.each { it.anonymize('beskrivning') }
        intyg.observationer?.each {
            it.anonymize('beskrivning')
            it.prognoser?.each {prognos -> prognos.anonymize('beskrivning')} 
        }
    }
    
}
