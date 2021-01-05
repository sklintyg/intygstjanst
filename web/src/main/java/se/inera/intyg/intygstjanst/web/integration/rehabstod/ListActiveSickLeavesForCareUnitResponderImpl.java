/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration.rehabstod;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;

import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ResultCodeEnum;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.integration.rehabstod.converter.SjukfallCertificateIntygsDataConverter;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsLista;

/**
 * Implements TjK for retrieving intygsdata for sjukfall.
 *
 * Created by eriklupander on 2016-02-02.
 */
public class ListActiveSickLeavesForCareUnitResponderImpl implements ListActiveSickLeavesForCareUnitResponderInterface {

    @Autowired
    private HsaService hsaService;

    @Autowired
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Override
    @PrometheusTimeMethod
    public ListActiveSickLeavesForCareUnitResponseType listActiveSickLeavesForCareUnit(String logicalAddress,
        ListActiveSickLeavesForCareUnitType parameters) {

        ListActiveSickLeavesForCareUnitResponseType response = new ListActiveSickLeavesForCareUnitResponseType();

        if (hasNoCareUnitId(parameters)) {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setComment("No careUnitHsaId specified in request.");
            return response;
        }

        final String careUnitHsaId = parameters.getEnhetsId().getExtension();
        final String careGiverHsaId = hsaService.getHsaIdForVardgivare(careUnitHsaId);
        final String personnummer = getPersonnummer(parameters);
        final int maxDagarSedanAvslut = getMaxDagarSedanAvslut(parameters);
        final List<String> hsaIdList = getHsaIdList(careUnitHsaId);

        final List<SjukfallCertificate> activeSjukfallCertificateForCareUnits =
            getSjukfallCertificates(careGiverHsaId, hsaIdList, personnummer, maxDagarSedanAvslut);

        return getResponse(response, activeSjukfallCertificateForCareUnits);
    }

    /**
     * Retrieves list of SjukfallCertificate based on the arguments and making sure that no test certificates are included.
     */
    private List<SjukfallCertificate> getSjukfallCertificates(String careGiverHsaId, List<String> hsaIdList, String personnummer,
        int maxDagarSedanAvslut) {
        final List<SjukfallCertificate> activeSjukfallCertificateForCareUnits;
        if (!Strings.isNullOrEmpty(personnummer)) {
            Personnummer pnr = Personnummer.createPersonnummer(personnummer)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
            activeSjukfallCertificateForCareUnits = sjukfallCertificateDao
                .findActiveSjukfallCertificateForPersonOnCareUnits(careGiverHsaId, hsaIdList, pnr.getPersonnummerWithDash(),
                    maxDagarSedanAvslut);
        } else {
            activeSjukfallCertificateForCareUnits = sjukfallCertificateDao
                .findActiveSjukfallCertificateForCareUnits(careGiverHsaId, hsaIdList, maxDagarSedanAvslut);
        }

        return activeSjukfallCertificateForCareUnits
                    .stream()
                    .filter(sjukfallCertificate -> !sjukfallCertificate.isTestCertificate())
                    .collect(Collectors.toList());
    }

    private boolean hasNoCareUnitId(ListActiveSickLeavesForCareUnitType parameters) {
        return parameters.getEnhetsId() == null || parameters.getEnhetsId().getExtension() == null
            || parameters.getEnhetsId().getExtension().trim().length() == 0;
    }

    private String getPersonnummer(ListActiveSickLeavesForCareUnitType parameters) {
        return parameters.getPersonId() != null && parameters.getPersonId().getExtension() != null
            ? parameters.getPersonId().getExtension().trim()
            : null;
    }

    private int getMaxDagarSedanAvslut(ListActiveSickLeavesForCareUnitType parameters) {
        return parameters.getMaxDagarSedanAvslut() != null ? parameters.getMaxDagarSedanAvslut() : 0;
    }

    private List<String> getHsaIdList(String careUnitHsaId) {
        List<String> hsaIdList = hsaService.getHsaIdForUnderenheter(careUnitHsaId);
        hsaIdList.add(careUnitHsaId); // add care unit HSAId to list
        return hsaIdList;
    }

    private ListActiveSickLeavesForCareUnitResponseType getResponse(ListActiveSickLeavesForCareUnitResponseType response,
        List<SjukfallCertificate> activeSjukfallCertificateForCareUnits) {
        response.setResultCode(ResultCodeEnum.OK);
        IntygsLista intygsLista = new IntygsLista();
        intygsLista.getIntygsData()
            .addAll(new SjukfallCertificateIntygsDataConverter().buildIntygsData(activeSjukfallCertificateForCareUnits));
        response.setIntygsLista(intygsLista);
        return response;
    }
}
