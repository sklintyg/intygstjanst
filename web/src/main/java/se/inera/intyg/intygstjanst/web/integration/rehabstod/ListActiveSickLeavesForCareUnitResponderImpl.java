package se.inera.intyg.intygstjanst.web.integration.rehabstod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ResultCodeEnum;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Arbetsformaga;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Diagnos;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Enhet;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Formaga;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsData;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsLista;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Patient;

/**
 * Created by eriklupander on 2016-02-02.
 */
public class ListActiveSickLeavesForCareUnitResponderImpl implements ListActiveSickLeavesForCareUnitResponderInterface {

    @Autowired
    private HsaService hsaService;

    @Autowired
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Override
    public ListActiveSickLeavesForCareUnitResponseType listActiveSickLeavesForCareUnit(String logicalAddress, ListActiveSickLeavesForCareUnitType parameters) {
        List<String> hsaIdList = hsaService.getHsaIdForUnderenheter(parameters.getEnhetsId().getExtension());
        hsaIdList.add(parameters.getEnhetsId().getExtension());

        List<SjukfallCertificate> activeSjukfallCertificateForCareUnits = sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(hsaIdList);

        ListActiveSickLeavesForCareUnitResponseType response = new ListActiveSickLeavesForCareUnitResponseType();
        response.setResultCode(ResultCodeEnum.OK);
        IntygsLista intygsLista = new IntygsLista();
        intygsLista.getIntygsData().addAll(buildIntygsData(activeSjukfallCertificateForCareUnits));
        response.setIntygsLista(intygsLista);
        return response;
    }

    // TODO move this into some factory...
    private List<IntygsData> buildIntygsData(List<SjukfallCertificate> sjukfallCertificates) {
        List<IntygsData> intygsDataList = new ArrayList<>();

        for (SjukfallCertificate sc : sjukfallCertificates) {
            IntygsData intygsData = new IntygsData();
            intygsData.setPatient(buildPatient(sc.getCivicRegistrationNumber(), sc.getPatientFirstName(), sc.getPatientLastName()));
            intygsData.setDiagnos(buildDiagnos(sc.getDiagnoseCode(), "TODO"));
            intygsData.setEnhet(buildEnhet(sc.getCareUnitId(), sc.getCareUnitName()));
            intygsData.setSkapadAv(buildHoSPerson(intygsData.getEnhet(), sc.getSigningDoctorName(), sc.getSigningDoctorId()));

            Arbetsformaga arbetsformaga = new Arbetsformaga();
            arbetsformaga.getFormaga().addAll(buildFormaga(sc.getSjukfallCertificateWorkCapacity()));
            intygsData.setArbetsformaga(arbetsformaga);

            intygsDataList.add(intygsData);
        }

        return intygsDataList;

    }

    private List<Formaga> buildFormaga(List<SjukfallCertificateWorkCapacity> workCapacities) {

        return workCapacities.stream().map(wc -> {
            Formaga formaga = new Formaga();
            formaga.setNedsattning(wc.getCapacityPercentage());
            formaga.setStartdatum(LocalDate.parse(wc.getFromDate()));
            formaga.setSlutdatum(LocalDate.parse(wc.getToDate()));
            return formaga;
        }).collect(Collectors.toList());

    }

    private Diagnos buildDiagnos(String grupp, String kod) {
        Diagnos diagnos = new Diagnos();
        diagnos.setGrupp(grupp);
        diagnos.setKod(kod);

        return diagnos;
    }

    private Patient buildPatient(String pnr, String fornamn, String efternamn) {
        Patient patient = new Patient();
        PersonId personId = new PersonId();
        personId.setExtension(pnr);
        patient.setPersonId(personId);
        patient.setFornamn(fornamn);
        patient.setEfternamn(efternamn);
        return patient;
    }

    private Formaga buildSjukskrivningsGrad(int value, Integer startOffset, Integer slutOffset) {
        Formaga sg2 = new Formaga();
        sg2.setNedsattning(value);
        sg2.setStartdatum(org.joda.time.LocalDate.now().plusWeeks(startOffset));
        sg2.setSlutdatum(org.joda.time.LocalDate.now().plusWeeks(slutOffset));
        return sg2;
    }

    private Enhet buildEnhet(String hsaId, String hsaName) {
        Enhet enhet = new Enhet();
        HsaId hsaIdType = new HsaId();
        hsaIdType.setExtension(hsaId);
        enhet.setEnhetsId(hsaIdType);
        enhet.setEnhetsnamn(hsaName);
        return enhet;
    }

    private HosPersonal buildHoSPerson(Enhet enhet, String namn, String hsaId) {
        HosPersonal hosPerson = new HosPersonal();
        hosPerson.setEnhet(enhet);
        hosPerson.setFullstandigtNamn(namn);
        HsaId hsaIdType = new HsaId();
        hsaIdType.setExtension(hsaId);
        hosPerson.setPersonalId(hsaIdType);
        return hosPerson;
    }
}
