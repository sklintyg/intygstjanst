/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateRelation;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.integration.converter.ConverterUtil;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RelationService;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * Internal REST endpoint for citizen oriented data. POST is used to not expose personal identities in URLs (and log files).
 */
@Path("/citizens")
public class CitizenController {

    public static final Set<String> EXCLUDED_CITIZEN_CERTIFICATES =
        new HashSet<>(Arrays.asList(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID));
    static final Logger LOG = LoggerFactory.getLogger(CitizenController.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private RelationService relationService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    //
    public static class RequestObject {

        private String id;
        private boolean archived;

        public String getId() {
            return id;
        }

        public boolean isArchived() {
            return archived;
        }

        public static RequestObject of(String id, boolean archived) {
            final RequestObject p = new RequestObject();
            p.id = id;
            p.archived = archived;
            return p;
        }
    }

    //
    public static class ResponseObject {

        private CertificateHolder certificate;
        private List<CertificateRelation> relations;

        public CertificateHolder getCertificate() {
            return certificate;
        }

        public List<CertificateRelation> getRelations() {
            return relations;
        }

        public void setRelations(List<CertificateRelation> relations) {
            this.relations = relations;
        }

        public static ResponseObject of(CertificateHolder certificate) {
            final ResponseObject r = new ResponseObject();
            r.certificate = certificate;
            return r;
        }
    }

    @PrometheusTimeMethod
    @POST
    @Path("/certificates")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<ResponseObject> getCertificates(@RequestBody RequestObject parameters) {

        final long t0 = System.currentTimeMillis();

        final Optional<Personnummer> pnr = Personnummer.createPersonnummer(parameters.getId());

        LOG.debug("List certificates for citizen, archived: {}", parameters.isArchived());

        if (!pnr.isPresent()) {
            return Collections.emptyList();
        }

        final List<ResponseObject> responseList = certificateService.listCertificatesForCitizen(pnr.get(),
            null, null, null)
            .stream()
            .filter(c -> filter(c, parameters.isArchived()))
            .map(ConverterUtil::toCertificateHolder)
            .map(ResponseObject::of)
            .peek(this::addRelations)
            .collect(Collectors.toList());

        // list is in ascending signed date order, and has to be reversed
        Collections.reverse(responseList);

        monitoringLogService.logCertificateListedByCitizen(pnr.orElse(null));

        LOG.info("Found {} certificates in {} ms", responseList.size(), (System.currentTimeMillis() - t0));

        return responseList;
    }

    private void addRelations(final ResponseObject response) {
        final List<CertificateRelation> relations = relationService.getRelationGraph(response.getCertificate().getId())
            .stream()
            .filter(this::accessibleForUser)
            .map(this::toCertificateRelation)
            .collect(Collectors.toList());
        response.setRelations(relations);
    }

    private boolean accessibleForUser(final Relation r) {
        try {
            return !certificateService.getCertificateForCare(r.getFromIntygsId()).isRevoked();
        } catch (InvalidCertificateException e) {
            throw new ServerException("Failed to get revoke status for related certificate: " + e);
        }
    }

    private CertificateRelation toCertificateRelation(final Relation relation) {
        final CertificateRelation cr = new CertificateRelation();
        cr.setFromIntygsId(relation.getFromIntygsId());
        cr.setToIntygsId(relation.getToIntygsId());
        cr.setRelationKod(RelationKod.fromValue(relation.getRelationKod()));
        cr.setSkapad(relation.getCreated());
        return cr;
    }

    // returns true for an accepted certificate (clear XML content)
    private boolean filter(final Certificate c, final boolean archived) {
        if (EXCLUDED_CITIZEN_CERTIFICATES.contains(c.getType())) {
            return false;
        }
        if (c.isDeleted() == archived && !c.isRevoked()) {
            c.setOriginalCertificate(null);
            return true;
        }
        return false;
    }
}
