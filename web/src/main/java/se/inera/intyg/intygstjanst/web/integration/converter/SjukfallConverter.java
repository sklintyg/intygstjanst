package se.inera.intyg.intygstjanst.web.integration.converter;

import org.springframework.stereotype.Service;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.PatientEnkel;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukskrivningsgrad;
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
            se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall sjukfall = new se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall();
            sjukfall.setDiagnoskod(sf.getDiagnosKod().getCleanedCode());

            HsaId enhetId = new HsaId();
            enhetId.setExtension(sf.getVardenhet().getId());
            sjukfall.setEnhetsId(enhetId);

            PatientEnkel patient = new PatientEnkel();
            PersonId personId = new PersonId();
            personId.setExtension(sf.getPatient().getId());
            patient.setPersonId(personId);
            patient.setFullstandigtNamn(sf.getPatient().getNamn());
            sjukfall.setPatient(patient);

            HsaId lakareHsaId = new HsaId();
            lakareHsaId.setExtension(sf.getLakare().getId());
            sjukfall.setPersonalId(lakareHsaId);

            sjukfall.setStartdatum(sf.getStart());
            sjukfall.setSlutdatum(sf.getSlut());
            sjukfall.setBrytdatum(sf.getStart().plusDays(minstaSjukskrivningslangd));

            sjukfall.setAntalIntyg(sf.getIntyg());
            sjukfall.setSjukskrivningslangd(sf.getDagar());

            Sjukskrivningsgrad sjukskrivningsGrad = new Sjukskrivningsgrad();
            sjukskrivningsGrad.setAktivGrad(sf.getAktivGrad());
            sjukskrivningsGrad.getGrader().addAll(sf.getGrader());
            sjukfall.setSjukskrivningsgrad(sjukskrivningsGrad);

            return sjukfall;
        }).collect(Collectors.toList());
    }

}
