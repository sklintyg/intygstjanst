package se.inera.intyg.intygstjanst.web.service.impl;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.intygstjanst.web.service.PuFilterService;
import se.inera.intyg.schemas.contract.Personnummer;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

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
    public void enrichWithPatientNameAndFilter(List<IntygData> sickLeaves, boolean filterProtectedPerson) {
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
                if (patientNotFound || personSvar.getPerson().isSekretessmarkering()) {

                    final var updatedName = patientNotFound ? SEKRETESS_SKYDDAD_NAME_UNKNOWN : SEKRETESS_SKYDDAD_NAME_PLACEHOLDER;
                    item.setPatientNamn(updatedName);

                    if (filterProtectedPerson) {
                        i.remove();
                    }

                } else if (personSvar.getPerson().isAvliden()) {
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
                .join(personSvar.getPerson().getFornamn(),
                        personSvar.getPerson().getMellannamn(),
                        personSvar.getPerson().getEfternamn());
    }
}

