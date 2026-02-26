/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ListSickLeavesForPersonResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ListSickLeavesForPersonResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ListSickLeavesForPersonType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ResultCodeEnum;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ResultType;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.csintegration.aggregator.ValidSickLeaveAggregator;
import se.inera.intyg.intygstjanst.web.integration.rehabstod.converter.SjukfallCertificateIntygsDataConverter;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsLista;

/**
 * @author Magnus Ekstrand on 2018-10-23.
 */
public class ListSickLeavesForPersonResponderImpl implements ListSickLeavesForPersonResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListSickLeavesForPersonResponderImpl.class);

    @Autowired
    private ValidSickLeaveAggregator validSickLeaveAggregator;

    @Autowired
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Override

    public ListSickLeavesForPersonResponseType listSickLeavesForPerson(
        String logicalAddress, ListSickLeavesForPersonType parameters) {

        ListSickLeavesForPersonResponseType response = new ListSickLeavesForPersonResponseType();

        try {
            Personnummer personnummer = parsePersonnummer(parameters);

            List<SjukfallCertificate> activeSjukfallCertificateForPerson = getSjukfallCertificate(personnummer);

            IntygsLista intygsLista = new IntygsLista();
            intygsLista.getIntygsData()
                .addAll(new SjukfallCertificateIntygsDataConverter().buildIntygsData(
                    activeSjukfallCertificateForPerson));

            response.setIntygsLista(intygsLista);
            response.setResult(createResultType(ResultCodeEnum.OK, null));

        } catch (Exception e) {
            LOGGER.error("Could not get active sick leaves for a person.", e);
            response.setResult(createResultType(ResultCodeEnum.ERROR, e.getMessage()));
        }

        return response;
    }

    /**
     * Retrieves list of SjukfallCertificate for the patient and making sure that no test certificates are included.
     */
    private List<SjukfallCertificate> getSjukfallCertificate(Personnummer personnummer) {
        final var sjukfallCertificate = sjukfallCertificateDao.findSjukfallCertificateForPerson(personnummer.getPersonnummerWithDash())
            .stream()
            .toList();

        return validSickLeaveAggregator.get(sjukfallCertificate);
    }

    private ResultType createResultType(ResultCodeEnum resultCode, String message) {
        ResultType result = new ResultType();
        result.setResultCode(resultCode);
        result.setResultMessage(message);
        return result;
    }

    private Personnummer parsePersonnummer(ListSickLeavesForPersonType parameters) {
        String personnummer = parameters.getPersonId() != null && parameters.getPersonId().getExtension() != null
            ? parameters.getPersonId().getExtension().trim()
            : null;
        return Personnummer.createPersonnummer(personnummer)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }
}