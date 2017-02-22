package se.inera.intyg.intygstjanst.web.integration.converter;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukskrivningsgrad;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukskrivningsgrader;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.CVType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by eriklupander on 2017-02-17.
 */
@Service
public class SjukfallConverter {

    public List<Sjukfall> toSjukfall(List<se.inera.intyg.infra.sjukfall.dto.Sjukfall> sjukfallList, int minstaSjukskrivningslangd) {
        return sjukfallList.stream().map(sf -> {
            se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall sjukfall =
                    new se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall();
            CVType diagnosKod = new CVType();
            diagnosKod.setCode(sf.getDiagnosKod().getCleanedCode());
            sjukfall.setDiagnoskod(diagnosKod);

            HsaId enhetId = new HsaId();
            enhetId.setExtension(sf.getVardenhet().getId());
            enhetId.setRoot("1.2.752.129.2.1.4.1");
            sjukfall.setEnhetsId(enhetId);

            Personnummer pnr = new Personnummer(sf.getPatient().getId());
            sjukfall.setPersonId(buildPersonId(pnr));
            sjukfall.setPatientFullstandigtNamn(sf.getPatient().getNamn());

            HsaId lakareHsaId = new HsaId();
            lakareHsaId.setRoot("1.2.752.129.2.1.4.1");
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
        personId.setRoot(pnr.isSamordningsNummer() ? "1.2.752.129.2.1.3.3" : "1.2.752.129.2.1.3.1");

        try {
            personId.setExtension(pnr.getNormalizedPnr());
        } catch (Exception e) {
            personId.setExtension(pnr.getPersonnummer());
        }

        return personId;
    }
}
