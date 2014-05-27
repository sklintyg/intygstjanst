package se.inera.certificate

import groovy.json.*

class AnonymiseraJson {
    
    AnonymiseraPersonId anonymiseraPersonId;
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

    AnonymiseraJson(AnonymiseraPersonId anonymiseraPersonId, AnonymiseraHsaId anonymiseraHsaId) {
        this.anonymiseraPersonId = anonymiseraPersonId
        this.anonymiseraHsaId = anonymiseraHsaId
    }
    
    String anonymiseraIntygsJson(String s) {
        def intyg = new JsonSlurper().parseText(s)
        anonymizeJson(intyg)
        JsonBuilder builder = new JsonBuilder( intyg )
        return builder.toString()
    }
    
    void anonymizeJson(def intyg) {
        intyg.patient.id.extension = anonymiseraPersonId.anonymisera(intyg.patient.id.extension)
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
