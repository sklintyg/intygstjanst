/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.schemas.contract.Personnummer;

@ExtendWith(MockitoExtension.class)
class PuFilterServiceImplTest {

    @Mock
    private PUService puService;

    @InjectMocks
    private PuFilterServiceImpl puFilterService;

    private static final String TOLVANSSON_PNR = "19121212-1212";
    private static final String TOLVANSSON_PNR_INVALID = "19121212-1211";
    private static final String VARDENHET_1 = "vg-1-ve-1";
    private static final String LAKARE1_HSA_ID = "lakare-1";
    private static final String LAKARE1_NAMN = "Läkare Läkarsson";

    @Test
    void testSekretessmarkeradIsNotFilteredWhenFilterOnProtectedPersonIsFalse() {
        mockPersonSvar(false, true);
        List<IntygData> list = buildIntygDataList(TOLVANSSON_PNR);

        puFilterService.enrichWithPatientNameAndFilter(list, LAKARE1_HSA_ID);

        assertEquals(1, list.size());
    }

    @Test
    void testSekretessmarkeradIsFilteredWhenFilterOnProtectedPersonIsTrue() {
        mockPersonSvar(false, true);
        final var list = buildIntygDataList(TOLVANSSON_PNR);

        puFilterService.enrichWithPatientNameAndFilter(list, null);
        assertEquals(0, list.size());
    }

    @Test
    void testDeceasedIsFilteredWhenFilterOnProtectedPersonIsFalse() {
        mockPersonSvar(true, false);
        List<IntygData> list = buildIntygDataList(TOLVANSSON_PNR);

        puFilterService.enrichWithPatientNameAndFilter(list, LAKARE1_HSA_ID);

        assertEquals(0, list.size());
    }

    @Test
    void testDeceasedIsFilteredWhenFilterOnProtectedPersonIsTrue() {
        mockPersonSvar(true, false);
        final var list = buildIntygDataList(TOLVANSSON_PNR);

        puFilterService.enrichWithPatientNameAndFilter(list, null);
        assertEquals(0, list.size());
    }

    @Test
    void testExceptionIsThrownWhenPersonSvarIncludesAnError() {
        mockPersonSvarError();
        assertThrows(
            IllegalStateException.class,
            () -> puFilterService.enrichWithPatientNameAndFilter(buildIntygDataList(TOLVANSSON_PNR), LAKARE1_HSA_ID)
        );
    }

    @Test
    void testNameIsAppliedFromPersonSvar() {
        mockPersonSvar(false, false);
        final var list = buildIntygDataList(TOLVANSSON_PNR);

        puFilterService.enrichWithPatientNameAndFilter(list, LAKARE1_HSA_ID);
        assertEquals("Fornamn Efternamn", list.getFirst().getPatientNamn());
    }

    @Test
    void testNameIsReplacedWhenSekretessmarkerad() {
        mockPersonSvar(false, true);
        final var list = buildIntygDataList(TOLVANSSON_PNR);

        puFilterService.enrichWithPatientNameAndFilter(list, LAKARE1_HSA_ID);

        assertEquals("Skyddad personuppgift", list.getFirst().getPatientNamn());
    }

    @Test
    void testNameIsReplacedByPlaceholderIfFromPersonSvarWasNotFound() {
        mockPersonSvarNotFound();
        final var list = buildIntygDataList(TOLVANSSON_PNR);

        puFilterService.enrichWithPatientNameAndFilter(list, LAKARE1_HSA_ID);
        assertEquals("Namn okänt", list.getFirst().getPatientNamn());
    }

    @Test
    void testFilterIsAppliedIfPersonSvarWasNotFoundAndFilterOnProtectedPersonTrue() {
        mockPersonSvarNotFound();
        final var list = buildIntygDataList(TOLVANSSON_PNR);

        puFilterService.enrichWithPatientNameAndFilter(list, null);

        assertEquals(0, list.size());
    }

    @Test
    void testFilterIsNotAppliedIfPersonSvarWasNotFoundAndFilterOnProtectedPersonFalse() {
        mockPersonSvarNotFound();
        final var list = buildIntygDataList(TOLVANSSON_PNR);

        puFilterService.enrichWithPatientNameAndFilter(list, LAKARE1_HSA_ID);

        assertEquals(1, list.size());
    }

    @Test
    void testEnrichPatientsWhenPersonnummerHasInvalidDigit() {
        assertThrows(
            IllegalStateException.class,
            () -> puFilterService.enrichWithPatientNameAndFilter(buildIntygDataList(TOLVANSSON_PNR_INVALID), LAKARE1_HSA_ID)
        );
    }

    private void mockPersonSvar(boolean avliden, boolean sekretess) {
        when(puService.getPersons(anyList()))
            .thenReturn(buildPersonMap(buildPersonSvar(TOLVANSSON_PNR, sekretess, avliden)));
    }

    private void mockPersonSvarError() {
        Map<Personnummer, PersonSvar> personSvarMap = new HashMap<>();
        personSvarMap.put(createPnr(TOLVANSSON_PNR), PersonSvar.error());
        when(puService.getPersons(anyList())).thenReturn(personSvarMap);
    }

    private void mockPersonSvarNotFound() {
        Map<Personnummer, PersonSvar> personSvarMap = new HashMap<>();
        personSvarMap.put(createPnr(TOLVANSSON_PNR), PersonSvar.notFound());
        when(puService.getPersons(anyList())).thenReturn(personSvarMap);
    }


    private PersonSvar buildPersonSvar(String pnr, boolean sekretess, boolean avliden) {
        return PersonSvar.found(buildPerson(pnr, sekretess, avliden));
    }

    private Person buildPerson(String pnr, boolean sekretess, boolean avliden) {
        return new Person(createPnr(pnr), sekretess, avliden, "Fornamn", null, "Efternamn",
            "Gatan 1", "11212", "Orten", false);
    }

    private List<IntygData> buildIntygDataList(String... personId) {
        return Arrays.stream(personId)
            .map(pnr -> buildIntygData(pnr, "Patient-" + pnr))
            .collect(Collectors.toList());
    }

    private IntygData buildIntygData(String id, String name) {
        final var intygData = new IntygData();
        intygData.setPatientNamn(name);
        intygData.setPatientId(id);
        intygData.setVardenhetId(VARDENHET_1);
        intygData.setLakareId(LAKARE1_HSA_ID);
        intygData.setLakareNamn(LAKARE1_NAMN);
        return intygData;
    }

    private Map<Personnummer, PersonSvar> buildPersonMap(PersonSvar... arr) {
        Map<Personnummer, PersonSvar> persons = new HashMap<>();
        Arrays.stream(arr).forEach(ps -> {
            Personnummer pnr = ps.getPerson() == null ? null : ps.getPerson().personnummer();
            persons.put(pnr, ps);
        });
        return persons;
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer
            .createPersonnummer(pnr)
            .orElseThrow(() -> new IllegalArgumentException("Cannot create Personnummer object with pnr: " + pnr));
    }
}
