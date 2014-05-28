package se.inera.certificate

import org.junit.Test

class AnonymiseraPersonIdTest {

    AnonymiseraPersonId anonymiseraPersonId = new AnonymiseraPersonId()

    @Test
    void anonymiseringGerInteSammaId() {
        String personId = "19121212-1212"
        String anonymiseradPersonId = anonymiseraPersonId.anonymisera(personId)
        assert personId != anonymiseradPersonId
    }

    @Test
    void anonymiseringGerSammaResultatFleraGånger() {
        String personId = "19121212-1212"
        String anonymiseradPersonId1 = anonymiseraPersonId.anonymisera(personId)
        String anonymiseradPersonId2 = anonymiseraPersonId.anonymisera(personId)
        assert anonymiseradPersonId1 == anonymiseradPersonId2
    }

    @Test
    void anonymiseringGerOlikaResultatFörOlikaId() {
        String personId1 = "19121212-1212"
        String personId2 = "20101010-2010"
        String anonymiseradPersonId1 = anonymiseraPersonId.anonymisera(personId1)
        String anonymiseradPersonId2 = anonymiseraPersonId.anonymisera(personId2)
        assert anonymiseradPersonId1 != anonymiseradPersonId2
    }
    
    @Test
    void anonymiseringAvFelaktigtPersonnr() {
        String personId = "20110043-6904"
        String anonymiseradPersonId1 = anonymiseraPersonId.anonymisera(personId)
        String anonymiseradPersonId2 = anonymiseraPersonId.anonymisera(personId)
        assert personId == anonymiseradPersonId1
        assert anonymiseradPersonId1 == anonymiseradPersonId2
    }

}
