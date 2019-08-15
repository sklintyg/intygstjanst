/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateRelation;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.integration.converter.ConverterUtil;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RelationService;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * Internal REST endpoint for citizen oriented data.
 */
@Path("/citizens")
public class CitizenController {

    static final List<String> EXCLUDED_CERTIFICATES = Arrays.asList(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID);

    static final Logger LOG = LoggerFactory.getLogger(CitizenController.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private RelationService relationService;

    @Autowired
    private MonitoringLogService monitoringLogService;


    @PrometheusTimeMethod
    @GET
    @Path("/{id}/certificates")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CertificateHolder> getCertificates(@PathParam("id") String id, @QueryParam("archived") boolean archived) {

        final long t0 = System.currentTimeMillis();

        final Optional<Personnummer> pnr = Personnummer.createPersonnummer(id);

        LOG.debug("List certificates for citizen, archived: {}", archived);

        if (!pnr.isPresent()) {
            return Collections.emptyList();
        }

        final List<CertificateHolder> certificateHolders = certificateService.listCertificatesForCitizen(pnr.get(),
            null, null, null)
            .stream()
            .filter(c -> filter(c, archived))
            .map(ConverterUtil::toCertificateHolder)
            .peek(this::addRelation)
            .collect(Collectors.toList());

        // list is in ascending signed date order, and has to be reversed
        Collections.reverse(certificateHolders);

        monitoringLogService.logCertificateListedByCitizen(pnr.orElse(null));

        LOG.info("Found {} certificates in {} ms", certificateHolders.size(), (System.currentTimeMillis() - t0));

        return certificateHolders;
    }

    private void addRelation(CertificateHolder ch) {
        final Optional<Relation> r = relationService.getParentRelation(ch.getId());
        if (r.isPresent()) {
            ch.setCertificateRelation(toCertificateRelation(r.get()));
        }
    }

    private CertificateRelation toCertificateRelation(Relation relation) {
        final CertificateRelation cr = new CertificateRelation();
        cr.setFromIntygsId(relation.getFromIntygsId());
        cr.setToIntygsId(relation.getToIntygsId());
        cr.setRelationKod(RelationKod.fromValue(relation.getRelationKod()));
        cr.setSkapad(relation.getCreated());
        return cr;
    }

    // returns true for an accepted certificate (clear XML content)
    private boolean filter(final Certificate c, final boolean archived) {
        switch (c.getType()) {
            case DbModuleEntryPoint.MODULE_ID:
            case DoiModuleEntryPoint.MODULE_ID:
                return false;
        }
        if (c.isDeleted() == archived && c.getStates().stream().noneMatch(h -> h.getState() == CertificateState.CANCELLED)) {
            c.setOriginalCertificate(null);
            return true;
        }
        return false;
    }
}
