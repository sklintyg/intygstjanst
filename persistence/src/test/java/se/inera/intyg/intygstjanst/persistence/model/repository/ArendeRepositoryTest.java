/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.persistence.model.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.intygstjanst.persistence.config.JpaConstans;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/persistence-config-unittest.xml" })
@Transactional
@ActiveProfiles({"dev","embedded"})
public class ArendeRepositoryTest {

    private static String ENHET_1_ID = "ENHET_1_ID";
    private static String ENHET_2_ID = "ENHET_2_ID";
    private static String ENHET_3_ID = "ENHET_3_ID";
    private static String ENHET_4_ID = "ENHET_4_ID";

    @PersistenceContext(unitName = JpaConstans.PERSISTANCE_UNIT_NAME)
    private EntityManager entityManager;

    @Autowired
    private ArendeRepository arendeRepository;

    @Test
    public void testFindOne() {
        Arende saved = buildFragaSvarFraga(ENHET_1_ID);
        arendeRepository.save(saved);
        Arende read = arendeRepository.findOne(saved.getInternReferens());
        assertEquals(read.getInternReferens(), saved.getInternReferens());
    }

    @Test
    public void testFindByMeddelandeId() {
        Arende saved = buildFragaSvarFraga(ENHET_1_ID);
        arendeRepository.save(saved);
        Arende read = arendeRepository.findByMeddelandeId(saved.getMeddelandeId());
        assertEquals(read.getInternReferens(), saved.getInternReferens());
    }

    @Test
    public void testFindAll() {
        arendeRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        arendeRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        arendeRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        arendeRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        arendeRepository.save(buildFragaSvarFraga(ENHET_4_ID));
        List<Arende> read = arendeRepository.findAll();
        assertEquals(read.size(), 5);
    }

    private Arende buildFragaSvarFraga(String logiskMottagare) {
        Arende arende = new Arende();
        arende.setIntygsId("intygsID");
        arende.setMeddelandeId(UUID.randomUUID().toString());
        arende.setReferens(ENHET_2_ID);
        arende.setTimeStamp(LocalDateTime.now());
        arende.setMeddelande("Meddelande");
        arende.setLogiskAdressmottagare(ENHET_3_ID);
        arende.setAmne("OVRIGT");
        return arende;
    }

}
