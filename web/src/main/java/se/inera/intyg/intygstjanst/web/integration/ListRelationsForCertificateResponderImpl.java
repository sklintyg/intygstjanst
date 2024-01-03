/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.common.support.Constants.KV_RELATION_CODE_SYSTEM;

import java.text.MessageFormat;
import java.util.List;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.inera.clinicalprocess.healthcond.certificate.types.v3.TypAvRelation;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.IntygRelations;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateType;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.RelationService;

/**
 * Created by eriklupander on 2017-05-11.
 */
@SchemaValidation(type = SchemaValidation.SchemaValidationType.IN)
public class ListRelationsForCertificateResponderImpl implements ListRelationsForCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListRelationsForCertificateResponderImpl.class);

    @Autowired
    private RelationService relationService;

    @Autowired
    private CertificateService certificateService;

    @Override
    @PrometheusTimeMethod
    public ListRelationsForCertificateResponseType listRelationsForCertificate(String logicalAddress,
        ListRelationsForCertificateType listRelationsForCertificateType) {
        long start = System.currentTimeMillis();
        ListRelationsForCertificateResponseType response = new ListRelationsForCertificateResponseType();
        for (String intygsId : listRelationsForCertificateType.getIntygsId()) {
            List<Relation> relationGraph = relationService.getRelationGraph(intygsId);
            response.getIntygRelation().add(buildIntygRelation(intygsId, relationGraph));
        }

        LOGGER.info("Loading relations for {} intygsId took {} ms", listRelationsForCertificateType.getIntygsId().size(),
            System.currentTimeMillis() - start);
        return response;
    }

    private IntygRelations buildIntygRelation(String intygsId, List<Relation> relationGraph) {
        IntygRelations intygRelations = new IntygRelations();
        intygRelations.setIntygsId(buildIntygId(intygsId));

        for (Relation r : relationGraph) {
            boolean makulerat;
            try {
                makulerat = certificateService.getCertificateForCare(r.getFromIntygsId()).isRevoked();
            } catch (InvalidCertificateException e) {
                LOGGER.error("Failed to get revoke status for certificate {}", r.getFromIntygsId());
                throw new ServerException(MessageFormat.format("Failed to get revoke status for certificate {}", r.getFromIntygsId()));
            }
            intygRelations.getRelation().add(convertRelation(r, makulerat));
        }
        return intygRelations;
    }

    private se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.Relation convertRelation(
        Relation r, boolean makulerat) {
        se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.Relation intygRelation =
            new se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.Relation();
        intygRelation.setFranIntygsId(buildIntygId(r.getFromIntygsId()));
        intygRelation.setTillIntygsId(buildIntygId(r.getToIntygsId()));
        intygRelation.setSkapad(r.getCreated());
        intygRelation.setTyp(buildTypAvRelation(r.getRelationKod()));
        intygRelation.setFranIntygMakulerat(makulerat);
        return intygRelation;
    }

    private TypAvRelation buildTypAvRelation(String relationKod) {
        TypAvRelation typAvRelation = new TypAvRelation();
        typAvRelation.setCode(relationKod);
        typAvRelation.setCodeSystem(KV_RELATION_CODE_SYSTEM);
        typAvRelation.setDisplayName(RelationKod.fromValue(relationKod).getKlartext());
        return typAvRelation;
    }

    private IntygId buildIntygId(String intygsId) {
        IntygId intygId = new IntygId();
        intygId.setRoot("");
        intygId.setExtension(intygsId);
        return intygId;
    }
}
