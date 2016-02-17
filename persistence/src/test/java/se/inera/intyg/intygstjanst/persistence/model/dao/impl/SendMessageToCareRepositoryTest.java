package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.intygstjanst.persistence.model.dao.SendMessageToCare;
import se.inera.intyg.intygstjanst.persistence.model.dao.SendMessageToCareRepository;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/persistence-config-unittest.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class SendMessageToCareRepositoryTest {
    
    private static String ENHET_1_ID = "ENHET_1_ID";
    private static String ENHET_2_ID = "ENHET_2_ID";
    private static String ENHET_3_ID = "ENHET_3_ID";
    private static String ENHET_4_ID = "ENHET_4_ID";
    
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private SendMessageToCareRepository sendMessageToCareRepository;

    @Test
    public void testFindOne() {
        SendMessageToCare saved = buildFragaSvarFraga(ENHET_1_ID);
        System.out.println(sendMessageToCareRepository==null);
        sendMessageToCareRepository.save(saved);
        SendMessageToCare read = sendMessageToCareRepository.findOne(saved.getInternReferens());
        assertEquals(read.getInternReferens(), saved.getInternReferens());
    }
    
    @Test
    public void testFindAll() {
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_4_ID));
        List<SendMessageToCare> read = sendMessageToCareRepository.findAll();
        assertEquals(read.size(), 5);
    }

    private SendMessageToCare buildFragaSvarFraga(String logiskMottagare) {
        SendMessageToCare sendMessageToCare = new SendMessageToCare();
        sendMessageToCare.setIntygsId("intygsID");
        sendMessageToCare.setMeddelandeId("meddelandeId");
        //sendMessageToCare.setFrageSigneringsDatum(LocalDateTime.now());
        sendMessageToCare.setMeddelande("");
        return sendMessageToCare;
    }
    
}
