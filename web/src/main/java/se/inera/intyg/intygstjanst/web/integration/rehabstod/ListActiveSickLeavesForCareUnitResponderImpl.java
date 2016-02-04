package se.inera.intyg.intygstjanst.web.integration.rehabstod;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ResultCodeEnum;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.integration.rehabstod.converter.SjukfallCertificateIntygsDataConverter;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsLista;

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
        ListActiveSickLeavesForCareUnitResponseType response = new ListActiveSickLeavesForCareUnitResponseType();

        if (parameters.getEnhetsId() == null || parameters.getEnhetsId().getExtension() == null) {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setComment("No careUnitHsaId specified in request.");
            return response;
        }

        String careUnitHsaId = parameters.getEnhetsId().getExtension();

        String careGiverHsaId = hsaService.getHsaIdForCareGiverOfCareUnit(careUnitHsaId);
        if (careGiverHsaId == null) {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setComment("No caregiver hsaId could be found for careunit hsaId '" + careUnitHsaId + "'.");
            return response;
        }

        List<String> hsaIdList = hsaService.getHsaIdForUnderenheter(careUnitHsaId);
        hsaIdList.add(careUnitHsaId);

        List<SjukfallCertificate> activeSjukfallCertificateForCareUnits = sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(hsaIdList, careGiverHsaId);

        response.setResultCode(ResultCodeEnum.OK);
        IntygsLista intygsLista = new IntygsLista();
        intygsLista.getIntygsData().addAll(new SjukfallCertificateIntygsDataConverter().buildIntygsData(activeSjukfallCertificateForCareUnits));
        response.setIntygsLista(intygsLista);
        return response;
    }


}
