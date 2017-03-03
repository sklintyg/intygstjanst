package se.inera.intyg.intygstjanst.web.integration.converter;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.schemas.contract.Personnummer;

import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukskrivningsgrad;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukskrivningsgrader;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts the output format from infra/sjukfall/engine {@link se.inera.intyg.infra.sjukfall.dto.Sjukfall} to
 * our rivta published service contract format {@link Sjukfall}.
 *
 * Created by eriklupander on 2017-02-17.
 */
@Service
public class SjukfallConverter {

    private static final String KODVERK_SAMORDNINGSNUMMER = "1.2.752.129.2.1.3.3";
    private static final String KODVERK_PERSONNUMMER = "1.2.752.129.2.1.3.1";
    private static final String KODVERK_HSAID = "1.2.752.129.2.1.4.1";

    public List<Sjukfall> toSjukfall(List<se.inera.intyg.infra.sjukfall.dto.Sjukfall> sjukfallList) {
        return sjukfallList.stream().map(sf -> {
            se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall sjukfall =
                    new se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall();
            se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Diagnoskod diagnoskod = new se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Diagnoskod();

            diagnoskod.setCode(sf.getDiagnosKod().getCleanedCode());
            sjukfall.setDiagnoskod(diagnoskod);

            HsaId enhetId = new HsaId();
            enhetId.setExtension(sf.getVardenhet().getId());
            enhetId.setRoot(KODVERK_HSAID);
            sjukfall.setEnhetsId(enhetId);

            Personnummer pnr = new Personnummer(sf.getPatient().getId());
            sjukfall.setPersonId(buildPersonId(pnr));
            sjukfall.setPatientFullstandigtNamn(sf.getPatient().getNamn());

            HsaId lakareHsaId = new HsaId();
            lakareHsaId.setRoot(KODVERK_HSAID);
            lakareHsaId.setExtension(sf.getLakare().getId());
            sjukfall.setPersonalId(lakareHsaId);

            sjukfall.setStartdatum(sf.getStart());
            sjukfall.setSlutdatum(sf.getSlut());

            sjukfall.setAntalIntyg(sf.getIntyg());
            sjukfall.setSjukskrivningslangd(sf.getDagar());

            Sjukskrivningsgrad sjukskrivningsGrad = new Sjukskrivningsgrad();
            sjukskrivningsGrad.setAktivGrad(sf.getAktivGrad());

            Sjukskrivningsgrader grader = new Sjukskrivningsgrader();
            grader.getGrad().addAll(sf.getGrader());
            sjukskrivningsGrad.setGrader(grader);
            sjukfall.setSjukskrivningsgrad(sjukskrivningsGrad);

            return sjukfall;
        }).collect(Collectors.toList());
    }

    private PersonId buildPersonId(Personnummer pnr) {
        PersonId personId = new PersonId();
        personId.setRoot(SamordningsnummerValidator.isSamordningsNummer(pnr) ? KODVERK_SAMORDNINGSNUMMER : KODVERK_PERSONNUMMER);

        try {
            personId.setExtension(pnr.getNormalizedPnr());
        } catch (Exception e) {
            personId.setExtension(pnr.getPersonnummer());
        }

        return personId;
    }
}
