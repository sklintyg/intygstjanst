package se.inera.intyg.intygstjanst.web.integration;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.IntygRelations;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateType;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.RelationService;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvRelation;

import java.util.List;

import static se.inera.intyg.common.support.Constants.KV_RELATION_CODE_SYSTEM;

/**
 * Created by eriklupander on 2017-05-11.
 */
@SchemaValidation
public class ListRelationsForCertificateResponderImpl implements ListRelationsForCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListRelationsForCertificateResponderImpl.class);

    @Autowired
    private RelationService relationService;

    @Override
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
            intygRelations.getRelation().add(convertRelation(r));
        }
        return intygRelations;
    }

    private se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.Relation convertRelation(Relation r) {
        se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.Relation intygRelation =
                new se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.Relation();
        intygRelation.setFranIntygsId(buildIntygId(r.getFromIntygsId()));
        intygRelation.setTillIntygsId(buildIntygId(r.getToIntygsId()));
        intygRelation.setSkapad(r.getCreated());
        intygRelation.setTyp(buildTypAvRelation(r.getRelationKod()));
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
