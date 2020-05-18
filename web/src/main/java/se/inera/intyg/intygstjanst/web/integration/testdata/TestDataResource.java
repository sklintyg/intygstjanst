package se.inera.intyg.intygstjanst.web.integration.testdata;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.w3._2000._09.xmldsig_.SignatureType;
import org.w3._2002._06.xmldsig_filter2.XPathType;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi2;
import se.inera.intyg.infra.testdata.TestDataTransformer;
import se.inera.intyg.intygstjanst.persistence.config.JpaConstants;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.service.RelationService;
import se.inera.intyg.intygstjanst.web.service.SjukfallCertificateService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.ObjectFactory;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.DatePeriodType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PQType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Relation;

@Path("/testdata")
public class TestDataResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDataResource.class);
//    protected static final String UTF_8_CHARSET = ";charset=utf-8";

    private TransactionTemplate transactionTemplate;

    private ObjectFactory objectFactory;
    private JAXBContext jaxbContext;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private SjukfallCertificateService sjukfallCertificateService;

    @Autowired
    private RelationService relationService;

    @PersistenceContext(unitName = JpaConstants.PERSISTANCE_UNIT_NAME)
    private EntityManager entityManager;

    @Autowired
    public void setTxManager(PlatformTransactionManager txManager) {
        this.transactionTemplate = new TransactionTemplate(txManager);
    }

    @PostConstruct
    public void initializeJaxbContext() throws JAXBException {
        // We need to register DatePeriodType with the JAXBContext explicitly for some reason.
        jaxbContext = JAXBContext.newInstance(RegisterCertificateType.class, DatePeriodType.class, SignatureType.class,
            XPathType.class, PartialDateType.class, PQType.class);
        objectFactory = new ObjectFactory();
    }

    @DELETE
    @Path("/")
    public Response deleteCertificates() {
        return transactionTemplate.execute(status -> {
            try {
                @SuppressWarnings("unchecked")
                List<Certificate> certificates = entityManager.createQuery("SELECT c FROM Certificate c").getResultList();
                for (Certificate certificate : certificates) {
                    if (certificate.getOriginalCertificate() != null) {
                        entityManager.remove(certificate.getOriginalCertificate());
                    }
                    entityManager.remove(certificate);
                }

                // Also delete any Relation
                List<se.inera.intyg.intygstjanst.persistence.model.dao.Relation> relations = entityManager
                    .createQuery("SELECT r FROM Relation r",
                        se.inera.intyg.intygstjanst.persistence.model.dao.Relation.class).getResultList();
                for (se.inera.intyg.intygstjanst.persistence.model.dao.Relation relation : relations) {
                    entityManager.remove(relation);
                }

                // Also delete any SjukfallCertificates
                List<SjukfallCertificate> sjukfallCertificates = entityManager
                    .createQuery("SELECT c FROM SjukfallCertificate c", SjukfallCertificate.class).getResultList();
                for (SjukfallCertificate sjukfallCert : sjukfallCertificates) {
                    entityManager.remove(sjukfallCert);
                }

                return Response.ok().build();
            } catch (Exception e) {
                status.setRollbackOnly();
                LOGGER.warn("delete all certificates failed", e);
                return Response.serverError().build();
            }
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response insertTestDataCertificate(TestDataWrapper cert) {
        JsonNode intygJson = TestDataTransformer.transformIntyg(cert.getData());
        JsonNode internalModelJson = intygJson.path("model");
        String intygsTyp = internalModelJson.path("typ").asText();
        String typVersion = internalModelJson.path("textVersion").asText();
        String internalModel = internalModelJson.toString();

        return transactionTemplate.execute(status -> {
            try {
                ModuleApi2 moduleApi = (ModuleApi2) moduleRegistry.getModuleApi(intygsTyp, typVersion);

                RegisterCertificateType request = moduleApi.getTransportFromInternal(internalModel);
                Intyg intyg = request.getIntyg();

                LOGGER.info("insert certificate {} ({})", intyg.getIntygsId(), intyg.getTyp().getCode());

                String xml = xmlToString(request);
//                ValidateXmlResponse validationResponse = moduleApi.validateXml(xml);
                String additionalInfo = moduleApi.getAdditionalInfo(intyg);

                Certificate certificate = toCertificate(intyg, intygsTyp, additionalInfo);

                OriginalCertificate originalCertificate = new OriginalCertificate(LocalDateTime.now(), xml,
                    certificate);

                certificate.setOriginalCertificate(originalCertificate);

                if (intyg.getRelation() != null) {
                    for (Relation rel : intyg.getRelation()) {
                        relationService.storeRelation(
                            new se.inera.intyg.intygstjanst.persistence.model.dao.Relation(certificate.getId(),
                                rel.getIntygsId().getExtension(),
                                rel.getTyp().getCode(), LocalDateTime.now()));
                    }
                }

                entityManager.persist(certificate);
                entityManager.persist(originalCertificate);

                sjukfallCertificateService.created(certificate);

                return Response.ok().build();
            } catch (Exception e) {
                status.setRollbackOnly();
                LOGGER.warn("insert certificate {} ({}) failed", "id(?)", intygsTyp, e);
                return Response.serverError().build();
            }
        });
    }

    private String xmlToString(RegisterCertificateType registerCertificate) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        JAXBElement<RegisterCertificateType> requestElement = objectFactory.createRegisterCertificate(registerCertificate);
        jaxbContext.createMarshaller().marshal(requestElement, stringWriter);
        return stringWriter.toString();
    }

    private Certificate toCertificate(Intyg intyg, String type, String additionalInfo) {
        Certificate certificate = new Certificate(intyg.getIntygsId().getExtension());

        certificate.setType(type);
        certificate.setTypeVersion(intyg.getVersion());
        certificate.setSigningDoctorName(intyg.getSkapadAv().getFullstandigtNamn());
        certificate.setSignedDate(intyg.getSigneringstidpunkt());

        certificate.setCareUnitId(intyg.getSkapadAv().getEnhet().getEnhetsId().getExtension());
        certificate.setCareUnitName(intyg.getSkapadAv().getEnhet().getEnhetsnamn());
        certificate.setCareGiverId(intyg.getSkapadAv().getEnhet().getVardgivare().getVardgivareId().getExtension());
        certificate.setCivicRegistrationNumber(createPnr(intyg));

        certificate.setAdditionalInfo(additionalInfo);
//
//        if (certificateHolder.getCertificateStates() != null) {
//            List<CertificateStateHistoryEntry> certificateStates = new ArrayList<>(
//                certificateHolder.getCertificateStates().size());
//            for (CertificateStateHolder certificateStateHolder : certificateHolder.getCertificateStates()) {
//                certificateStates
//                    .add(new CertificateStateHistoryEntry(certificateStateHolder.getTarget(), certificateStateHolder.getState(),
//                        certificateStateHolder.getTimestamp()));
//            }
//            certificate.setStates(certificateStates);
//        }
        return certificate;
    }

    private Personnummer createPnr(Intyg intyg) {
        String personId = null;
        try {
            personId = intyg.getPatient().getPersonId().getExtension();
        } catch (NullPointerException npe) {
            throw new RuntimeException("Could not get patient's personnummer from intyg");
        }

        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));

    }

    private static class TestDataWrapper {

        public JsonNode data;

        public JsonNode getData() {
            return data;
        }

        public void setData(JsonNode data) {
            this.data = data;
        }
    }
}
