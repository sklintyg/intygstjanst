package se.inera.intyg.intygstjanst.web.integration;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.Sjukfall;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.integration.converter.SjukfallCertificateConverter;
import se.inera.intyg.intygstjanst.web.integration.converter.SjukfallConverter;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.SjukfallLista;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

/**
 * Created by eriklupander on 2017-02-15.
 */
@SchemaValidation
public class ListSickLeavesForCareResponderImpl implements ListSickLeavesForCareResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListSickLeavesForCareResponderImpl.class);
    private static final int MAX_LEN = Integer.MAX_VALUE;

    @Autowired
    private HsaService hsaService;

    @Autowired
    private SjukfallEngineService sjukfallEngineService;

    @Autowired
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Autowired
    private SjukfallConverter sjukfallConverter;

    @Autowired
    private SjukfallCertificateConverter sjukfallCertificateConverter;

    @Override
    public ListSickLeavesForCareResponseType listSickLeavesForCare(String logicalAddress, ListSickLeavesForCareType params) {

        ListSickLeavesForCareResponseType responseType = new ListSickLeavesForCareResponseType();

        try {
            if (params.getEnhetsId() == null || StringUtils.isEmpty(params.getEnhetsId().getExtension())) {
                throw new ServerException("Request to ListSickLeavesForCare is missing required parameter 'enhets-id'");
            }

            if (params.getMaxDagarMellanIntyg() < 0) {
                throw new ServerException("Request to ListSickLeavesForCare has invalid value for parameter 'maxDagarMellanIntyg', "
                        + "must be >= 0, was " + params.getMaxDagarMellanIntyg());
            }

            int maxSjukskrivningslangd = params.getMaxSjukskrivningslangd() != null ? params.getMaxSjukskrivningslangd() : MAX_LEN;
            int minstaSjukskrivningslangd = params.getMinstaSjukskrivningslangd() != null ? params.getMinstaSjukskrivningslangd() : 0;

            // Set up list of enhet hsaId's to query for.
            List<String> hsaIdList = hsaService.getHsaIdForUnderenheter(params.getEnhetsId().getExtension());
            hsaIdList.add(params.getEnhetsId().getExtension());

            // Load sjukfallCertificates from DAO
            List<SjukfallCertificate> certificates = sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(hsaIdList);

            // Convert to the IntygData format that the SjukfallEngine accepts.
            List<IntygData> intygDataList = sjukfallCertificateConverter.convert(certificates);

            // Feed the intygdata into the sjukfallengine
            IntygParametrar sjukfallEngineParams = new IntygParametrar(params.getMaxDagarMellanIntyg(), LocalDate.now());
            List<Sjukfall> sjukfall = sjukfallEngineService.beraknaSjukfall(intygDataList, sjukfallEngineParams);

            // Perform post-processing filtering of sjukskrivningslängder and läkare.
            List<HsaId> lakareList = params.getPersonalId().stream().filter(Objects::nonNull).collect(Collectors.toList());
            sjukfall = sjukfall.stream()
                    .filter(sf -> sf.getDagar() >= minstaSjukskrivningslangd)
                    .filter(sf -> sf.getDagar() < maxSjukskrivningslangd)
                    .filter(sf -> lakareList == null || lakareList.size() == 0
                            || lakareList.stream()
                                    .map(id -> id.getExtension())
                                    .anyMatch(extension -> extension.equals(sf.getLakare().getId())))
                    .collect(Collectors.toList());

            // Transform the output of the sjukfallengine into rivta TjK format and build the response object.
            SjukfallLista sjukfallLista = new SjukfallLista();
            sjukfallLista.getSjukfall().addAll(sjukfallConverter.toSjukfall(sjukfall));

            ResultType resultType = new ResultType();
            resultType.setResultCode(ResultCodeType.OK);
            responseType.setResult(resultType);
            responseType.setSjukfallLista(sjukfallLista);
            return responseType;
        } catch (ServerException e) {
            LOGGER.error("Caught ServerException serving ListSickLeavesForCare: " + e.getMessage());
            ResultType resultType = new ResultType();
            resultType.setResultCode(ResultCodeType.ERROR);
            resultType.setResultText(e.getMessage());
            responseType.setResult(resultType);
            return responseType;
        }
    }
}
