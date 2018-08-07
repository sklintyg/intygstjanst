/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.converter.SjukfallCertificateConverter;
import se.inera.intyg.intygstjanst.web.integration.converter.SjukfallConverter;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.SjukfallLista;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @PrometheusTimeMethod
    public ListSickLeavesForCareResponseType listSickLeavesForCare(String logicalAddress, ListSickLeavesForCareType params) {

        ListSickLeavesForCareResponseType responseType = new ListSickLeavesForCareResponseType();

        if (params.getEnhetsId() == null || StringUtils.isEmpty(params.getEnhetsId().getExtension())) {
            throw new IllegalArgumentException("Request to ListSickLeavesForCare is missing required parameter 'enhets-id'");
        }

        if (params.getMaxDagarMellanIntyg() < 0) {
            throw new IllegalArgumentException("Request to ListSickLeavesForCare has invalid value for parameter 'maxDagarMellanIntyg', "
                    + "must be >= 0, was " + params.getMaxDagarMellanIntyg());
        }

        int maxSjukskrivningslangd = params.getMaxSjukskrivningslangd() != null ? params.getMaxSjukskrivningslangd() : MAX_LEN;
        int minstaSjukskrivningslangd = params.getMinstaSjukskrivningslangd() != null ? params.getMinstaSjukskrivningslangd() : 0;

        // Set up list of enhet hsaId's to query for.
        String careUnitHsaId = params.getEnhetsId().getExtension();
        String careGiverHsaId = hsaService.getHsaIdForVardgivare(careUnitHsaId);

        List<String> hsaIdList = hsaService.getHsaIdForUnderenheter(careUnitHsaId);
        hsaIdList.add(careUnitHsaId); // also add care unit's HsaId to list

        // Load sjukfallCertificates from DAO
        List<SjukfallCertificate> certificates = sjukfallCertificateDao
                .findActiveSjukfallCertificateForCareUnits(careGiverHsaId, hsaIdList);

        // Convert to the IntygData format that the SjukfallEngine accepts.
        List<IntygData> intygDataList = sjukfallCertificateConverter.convert(certificates);

        // Feed the intygdata into the sjukfallengine
        IntygParametrar sjukfallEngineParams = new IntygParametrar(params.getMaxDagarMellanIntyg(), LocalDate.now());
        List<SjukfallEnhet> sjukfall = sjukfallEngineService.beraknaSjukfallForEnhet(intygDataList, sjukfallEngineParams);

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

        responseType.setSjukfallLista(sjukfallLista);
        return responseType;
    }
}
