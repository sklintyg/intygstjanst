package se.inera.intyg.intygstjanst.web.integration;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.sjukfall.dto.Formaga;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.LangdIntervall;
import se.inera.intyg.infra.sjukfall.dto.Sjukfall;
import se.inera.intyg.infra.sjukfall.services.SjukfallService;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.SjukfallLista;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by eriklupander on 2017-02-15.
 */
@SchemaValidation
public class ListSickLeavesForCareResponderImpl implements ListSickLeavesForCareResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListSickLeavesForCareResponderImpl.class);

    @Autowired
    private HsaService hsaService;

    @Autowired
    private SjukfallService sjukfallService;

    @Autowired
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Override
    public ListSickLeavesForCareResponseType listSickLeavesForCare(String logicalAddress, ListSickLeavesForCareType params) {

        List<String> hsaIdList = hsaService.getHsaIdForUnderenheter(params.getEnhetsId().getExtension());
        hsaIdList.add(params.getEnhetsId().getExtension());

        List<SjukfallCertificate> certificates = sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(hsaIdList);

        IntygParametrar sjukfallEngineParams = new IntygParametrar(null, new LangdIntervall(Integer.toString(params.getMinstaSjukskrivningslangd().intValue()), "999"), params.getMaxIntygsGlapp().intValue(), LocalDate.now());
        List<Sjukfall> sjukfall = sjukfallService.beraknaSjukfall(convert(certificates), sjukfallEngineParams);

        SjukfallLista sjukfallLista = new SjukfallLista();
        sjukfallLista.getSjukfall().addAll(toSjukfall(sjukfall, params.getMinstaSjukskrivningslangd().intValue()));

        ListSickLeavesForCareResponseType responseType = new ListSickLeavesForCareResponseType();
        ResultType resultType = new ResultType();
        resultType.setResultCode(ResultCodeType.OK);
        responseType.setResult(resultType);
        responseType.setIntygsLista(sjukfallLista);
        return responseType;
    }

    private List<se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall> toSjukfall(List<Sjukfall> sjukfallList, int minstaSjukskrivningslangd) {
         return sjukfallList.stream().map(sf -> {
             se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall sjukfall = new se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall();
             sjukfall.setDiagnoskod(sf.getDiagnosKod().getId());

             HsaId enhetId = new HsaId();
             enhetId.setExtension(sf.getLakare().getVardenhet().getId());
             sjukfall.setEnhetId(enhetId);

             Patient patient = new Patient();
             PersonId personId = new PersonId();
             personId.setExtension(sf.getPatient().getId());
             patient.setPersonId(personId);
             patient.setFullstandigtNamn(sf.getPatient().getNamn());
             sjukfall.setPatient(patient);

             HsaId lakareHsaId = new HsaId();
             lakareHsaId.setExtension(sf.getLakare().getId());
             sjukfall.setSkapadAv(lakareHsaId);

             sjukfall.setStartdatum(sf.getStart());
             sjukfall.setSlutdatum(sf.getSlut());
             sjukfall.setBrytdatum(sf.getStart().plusDays(minstaSjukskrivningslangd));
             
             return sjukfall;
         }).collect(Collectors.toList());
    }

    private List<IntygData> convert(List<SjukfallCertificate> list) {
        return list.stream().map(sc -> {
            IntygData intyg = new IntygData();
            intyg.setIntygId(intyg.getIntygId());
            intyg.setEnkeltIntyg(false);
            intyg.setFormagor(buildFormaga(sc.getSjukfallCertificateWorkCapacity()));
            intyg.setDiagnosKod(sc.getDiagnoseCode());
            intyg.setLakareId(sc.getSigningDoctorId());
            intyg.setLakareNamn(sc.getSigningDoctorName());
            intyg.setSigneringsTidpunkt(sc.getSigningDateTime());
            intyg.setPatientId(sc.getCivicRegistrationNumber());
            intyg.setPatientNamn(sc.getPatientName());
            intyg.setVardenhetId(sc.getCareUnitId());
            intyg.setVardenhetNamn(sc.getCareUnitName());
            return intyg;
        }).collect(Collectors.toList());
    }

    private List<Formaga> buildFormaga(List<SjukfallCertificateWorkCapacity> workCapacities) {

        return workCapacities.stream()
                .map(this::buildFormaga)
                .collect(Collectors.toList());
    }

    private Formaga buildFormaga(SjukfallCertificateWorkCapacity wc) {
        return new Formaga(LocalDate.parse(wc.getFromDate()), LocalDate.parse(wc.getToDate()), wc.getCapacityPercentage());
    }
}
