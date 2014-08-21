package se.inera.certificate

import org.junit.Test

class AnonymiseraPersonIdTest {

    AnonymiseraPersonId anonymiseraPersonId = new AnonymiseraPersonId() {
        @Override
        protected int getRandomIndex() {
            return 0;
        }
    }

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
    void anonymiseringGerSammaResultatFörOlikaIdNärTestPnrTarSlut() {
        String personId1 = "19121212-1212"
        String personId2 = "20101010-2010"
        String personId3 = "20111111-1111"
        String anonymiseradPersonId1 = anonymiseraPersonId.anonymisera(personId1)
        String anonymiseradPersonId2 = anonymiseraPersonId.anonymisera(personId2)
        String anonymiseradPersonId3 = anonymiseraPersonId.anonymisera(personId3)
        assert anonymiseradPersonId1 == anonymiseradPersonId3
    }
    
    @Test
    void normaliseraLäggerTilBindestreck() {
        String personId1 = "191212121212"
        String personId2 = "20101010-2010"
        String normaliseradPersonId1 = anonymiseraPersonId.normalisera(personId1)
        String normaliseradPersonId2 = anonymiseraPersonId.normalisera(personId2)
        assert normaliseradPersonId1 == "19121212-1212"
        assert normaliseradPersonId2 == personId2
    }
    
}
