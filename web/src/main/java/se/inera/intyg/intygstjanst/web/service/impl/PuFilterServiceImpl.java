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

import com.google.common.base.Joiner;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.intygstjanst.web.service.PuFilterService;
import se.inera.intyg.schemas.contract.Personnummer;

@Service
public class PuFilterServiceImpl implements PuFilterService {

    private static final String SEKRETESS_SKYDDAD_NAME_PLACEHOLDER = "Skyddad personuppgift";
    private static final String SEKRETESS_SKYDDAD_NAME_UNKNOWN = "Namn okänt";

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private final PUService puService;

    public PuFilterServiceImpl(PUService puService) {
        this.puService = puService;
    }

    @Override
    public void enrichWithPatientNameAndFilter(List<IntygData> sickLeaves, String protectedPersonFilterId) {
        final var personSvarMap = fetchPersons(sickLeaves);

        final var i = sickLeaves.iterator();
        while (i.hasNext()) {
            final var item = i.next();

            final var pnr = getPersonnummerOfOptional(item.getPatientId());
            if (pnr.isEmpty()) {
                i.remove();
                LOG.warn("Problem parsing personnummer returned by PU service. Removing from list of sjukfall.");
                continue;
            }

            final var personSvar = personSvarMap.get(pnr.get());
            final var patientNotFound = personSvar.getStatus() == PersonSvar.Status.NOT_FOUND;
            if (personSvar.getStatus() == PersonSvar.Status.FOUND || patientNotFound) {
                if (patientNotFound || personSvar.getPerson().sekretessmarkering()) {

                    final var updatedName = patientNotFound ? SEKRETESS_SKYDDAD_NAME_UNKNOWN : SEKRETESS_SKYDDAD_NAME_PLACEHOLDER;
                    item.setPatientNamn(updatedName);

                    if (protectedPersonFilterId == null || !protectedPersonFilterId.equals(item.getLakareId())) {
                        i.remove();
                    }

                } else if (personSvar.getPerson().avliden()) {
                    i.remove();
                } else if (joinNames(personSvar).equals("")) {
                    item.setPatientNamn(SEKRETESS_SKYDDAD_NAME_UNKNOWN);
                } else {
                    item.setPatientNamn(joinNames(personSvar));
                }
            } else if (personSvar.getStatus() == PersonSvar.Status.ERROR) {
                throw new IllegalStateException("Could not contact PU service, not showing any sjukfall.");
            } else {
                item.setPatientNamn(SEKRETESS_SKYDDAD_NAME_UNKNOWN);
            }
        }
    }

    private Map<Personnummer, PersonSvar> fetchPersons(List<IntygData> sjukfallList) {
        if (sjukfallList.isEmpty()) {
            return new HashMap<>();
        }

        final var personSvarMap = puService.getPersons(this.getPersonnummerListFromIntygDataList(sjukfallList));

        if (personSvarMap.isEmpty()) {
            throw new IllegalStateException("Could not contact PU service, not showing any sjukfall.");
        }
        return personSvarMap;
    }

    private List<Personnummer> getPersonnummerListFromIntygDataList(List<IntygData> list) {
        return getPersonnummerListOfOptionalsFromList(list).stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .distinct()
            .collect(Collectors.toList());
    }

    private List<Optional<Personnummer>> getPersonnummerListOfOptionalsFromList(List<IntygData> list) {
        return list.stream()
            .map(se -> getPersonnummerOfOptional(se.getPatientId()))
            .collect(Collectors.toList());
    }

    private Optional<Personnummer> getPersonnummerOfOptional(String pnr) {
        Personnummer personnummer = null;
        try {
            personnummer = getPersonnummer(pnr);
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }

        return Optional.ofNullable(personnummer);
    }

    private Personnummer getPersonnummer(String pnr) {
        final var personnummer = Personnummer.createPersonnummer(pnr)
            .orElseThrow(() -> new RuntimeException("Found unparsable personnummer '" + pnr + "'"));

        if (!personnummer.verifyControlDigit()) {
            throw new RuntimeException("Found personnummer '" + personnummer.getPersonnummerHash() + "' with invalid control digit");
        }

        return personnummer;
    }

    private String joinNames(PersonSvar personSvar) {
        return Joiner.on(' ').skipNulls()
            .join(personSvar.getPerson().fornamn(),
                personSvar.getPerson().mellannamn(),
                personSvar.getPerson().efternamn());
    }
}
