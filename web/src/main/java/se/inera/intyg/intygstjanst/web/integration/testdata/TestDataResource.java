package se.inera.intyg.intygstjanst.web.integration.testdata;

import static se.inera.intyg.common.support.Constants.KV_AMNE_CODE_SYSTEM;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi2;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.infra.testdata.TestDataTransformer;
import se.inera.intyg.intygstjanst.persistence.config.JpaConstants;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.persistence.model.dao.ArendeRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.service.RelationService;
import se.inera.intyg.intygstjanst.web.service.SjukfallCertificateService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.ObjectFactory;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.Komplettering;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.SkickatAv;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.DatePeriodType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PQType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.MeddelandeReferens;
import se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare;

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

    @Autowired
    private ArendeRepository arendeRepository;

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
    @Path("/cert/")
    public Response deleteCertificates() {
        return transactionTemplate.execute(status -> {
            try {
                var certificates = entityManager.createQuery("SELECT c FROM Certificate c", Certificate.class).getResultList();
                for (var certificate : certificates) {
                    if (certificate.getOriginalCertificate() != null) {
                        entityManager.remove(certificate.getOriginalCertificate());
                    }
                    entityManager.remove(certificate);
                }

                var relationList = entityManager
                    .createQuery("SELECT r FROM Relation r",
                        se.inera.intyg.intygstjanst.persistence.model.dao.Relation.class).getResultList();
                for (var relation : relationList) {
                    entityManager.remove(relation);
                }

                var sjukfallCertificateList = entityManager
                    .createQuery("SELECT c FROM SjukfallCertificate c", SjukfallCertificate.class).getResultList();
                for (var sjukfallCert : sjukfallCertificateList) {
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
    @Path("/cert/")
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
                String additionalInfo = moduleApi.getAdditionalInfo(intyg);

                Certificate certificate = toCertificate(intyg, intygsTyp, additionalInfo);

                OriginalCertificate originalCertificate = new OriginalCertificate(LocalDateTime.now(), xml,
                    certificate);

                certificate.setOriginalCertificate(originalCertificate);

                if (intyg.getRelation() != null) {
                    for (var rel : intyg.getRelation()) {
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
        var certificate = new Certificate(intyg.getIntygsId().getExtension());

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

        return createPnr(personId);

    }

    private static Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + personId));
    }

    @DELETE
    @Path("/arende/")
    public Response deleteArende() {
        return transactionTemplate.execute(status -> {
            try {
                var arendeList = entityManager.createQuery("SELECT a FROM Arende a", Arende.class).getResultList();
                for (var arende : arendeList) {
                    entityManager.remove(arende);
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
    @Path("/arende/")
    public Response insertArende(TestDataWrapper data) {
        JsonNode arendeJson = TestDataTransformer.transformIntyg(data.getData());
        var arende = new Arende();

        String skickatAv = safeTextGet(arendeJson, "skickatAv");
        if ("WC".equals(skickatAv)) {
            var sendMessageToRecipientType = toSendMessageToRecipientType(arendeJson);
            var xmlString = toXml(sendMessageToRecipientType);
            buildArende(arendeJson, arende, xmlString);
        } else {
            var sendMessageToCareType = toSendMessageToCareType(arendeJson);
            var xmlString = toXml(sendMessageToCareType);
            buildArende(arendeJson, arende, xmlString);
        }

        arendeRepository.save(arende);
        return Response.ok().build();
    }

    private void buildArende(JsonNode arendeJson, Arende arende, String xmlString) {
        arende.setAmne(safeTextGet(arendeJson, "amne"));
        arende.setInternReferens(0L);
        arende.setIntygsId(safeTextGet(arendeJson, "intygsId"));
        arende.setLogiskAdressmottagare("mottagare");
        arende.setMeddelande(xmlString);
        arende.setMeddelandeId(safeTextGet(arendeJson, "meddelandeId"));
        arende.setReferens(safeTextGet(arendeJson, "referensId"));
        arende.setTimeStamp(safeDateTimeGet(arendeJson, "timestamp"));
    }

    private static SendMessageToRecipientType toSendMessageToRecipientType(JsonNode jsonData) {
        var sendMessageToRecipientType = new SendMessageToRecipientType();

        Amneskod amnesKod = buildAmneskod(jsonData);
        sendMessageToRecipientType.setAmne(amnesKod);

        IntygId intygId = buildIntygId(jsonData);
        sendMessageToRecipientType.setIntygsId(intygId);

        sendMessageToRecipientType.setLogiskAdressMottagare("TestData");
        sendMessageToRecipientType.setMeddelande(safeTextGet(jsonData, "meddelande"));
        sendMessageToRecipientType.setMeddelandeId(safeTextGet(jsonData, "meddelandeId"));
        sendMessageToRecipientType.setPaminnelseMeddelandeId(safeTextGet(jsonData, "paminnelseMeddelandeId"));
        sendMessageToRecipientType.setReferensId(safeTextGet(jsonData, "referensId"));
        sendMessageToRecipientType.setRubrik(safeTextGet(jsonData, "rubrik"));

        sendMessageToRecipientType.setSistaDatumForSvar(safeDateGet(jsonData, "sistaDatumForSvar"));
        sendMessageToRecipientType.setSkickatTidpunkt(safeDateTimeGet(jsonData, "skickatTidpunkt"));

        var personNr = InternalConverterUtil.getPersonId(createPnr(safeTextGet(jsonData, "patientPersonId")));
        sendMessageToRecipientType.setPatientPersonId(personNr);

        HosPersonal skickatAv = buildSkickatAv(jsonData);
        sendMessageToRecipientType.setSkickatAv(skickatAv);

        MeddelandeReferens svarPa = buildSvarPa(jsonData);
        sendMessageToRecipientType.setSvarPa(svarPa);

        return sendMessageToRecipientType;
    }

    private static SendMessageToCareType toSendMessageToCareType(JsonNode jsonData) {
        var sendMessageToCareType = new SendMessageToCareType();

        Amneskod amnesKod = buildAmneskod(jsonData);
        sendMessageToCareType.setAmne(amnesKod);

        IntygId intygId = buildIntygId(jsonData);
        sendMessageToCareType.setIntygsId(intygId);

        sendMessageToCareType.setLogiskAdressMottagare("TestData");
        sendMessageToCareType.setMeddelande(safeTextGet(jsonData, "meddelande"));
        sendMessageToCareType.setMeddelandeId(safeTextGet(jsonData, "meddelandeId"));
        sendMessageToCareType.setPaminnelseMeddelandeId(safeTextGet(jsonData, "paminnelseMeddelandeId"));
        sendMessageToCareType.setReferensId(safeTextGet(jsonData, "referensId"));
        sendMessageToCareType.setRubrik(safeTextGet(jsonData, "rubrik"));

        sendMessageToCareType.setSistaDatumForSvar(safeDateGet(jsonData, "sistaDatumForSvar"));
        sendMessageToCareType.setSkickatTidpunkt(safeDateTimeGet(jsonData, "skickatTidpunkt"));

        var personNr = InternalConverterUtil.getPersonId(createPnr(safeTextGet(jsonData, "patientPersonId")));
        sendMessageToCareType.setPatientPersonId(personNr);

        SkickatAv skickatAv = new SkickatAv();
        String info = "testDataInfo";
        skickatAv.getKontaktInfo().add(info);
        sendMessageToCareType.setSkickatAv(skickatAv);

        Komplettering komp = new Komplettering();
        komp.setText("testdata");
        komp.setFrageId("testdata");
        komp.setInstans(0);
        sendMessageToCareType.getKomplettering().add(komp);

        MeddelandeReferens svarPa = buildSvarPa(jsonData);
        sendMessageToCareType.setSvarPa(svarPa);

        return sendMessageToCareType;
    }

    private static Amneskod buildAmneskod(JsonNode jsonData) {
        var amnesKod = new Amneskod();
        amnesKod.setCode(safeTextGet(jsonData, "amne"));
        amnesKod.setCodeSystem(KV_AMNE_CODE_SYSTEM);
        amnesKod.setDisplayName(safeTextGet(jsonData, "amne"));
        return amnesKod;
    }

    private static IntygId buildIntygId(JsonNode jsonData) {
        var intygId = new IntygId();
        intygId.setRoot(safeTextGet(jsonData, "enhetId"));
        intygId.setExtension(safeTextGet(jsonData, "intygsId"));
        return intygId;
    }

    private static MeddelandeReferens buildSvarPa(JsonNode jsonData) {
        var svarPa = new MeddelandeReferens();
        svarPa.setMeddelandeId(safeTextGet(jsonData, "svarPaId"));
        svarPa.setReferensId(safeTextGet(jsonData, "svarPaReferens"));
        return svarPa;
    }

    private static HosPersonal buildSkickatAv(JsonNode jsonData) {
        var skickatAv = new HosPersonal();
        Enhet enhet = buildEnhet(jsonData);
        skickatAv.setEnhet(enhet);

//        skickatAv.setForskrivarkod(jsonData.get("").textValue());

        HsaId personalId = InternalConverterUtil.getHsaId(safeTextGet(jsonData, "signeratAv"));
        skickatAv.setPersonalId(personalId);
        skickatAv.setFullstandigtNamn(safeTextGet(jsonData, "signeratAvName"));
        return skickatAv;
    }

    private static Enhet buildEnhet(JsonNode jsonData) {
        Enhet enhet = new Enhet();
        ArbetsplatsKod arbetsPlatsKod = null;
//        arbetsPlatsKod.setRoot(jsonData.get("").textValue());
//        arbetsPlatsKod.setExtension(jsonData.get("").textValue());
        enhet.setArbetsplatskod(arbetsPlatsKod);

        enhet.setEnhetsId(InternalConverterUtil.getHsaId(safeTextGet(jsonData, "enhetId")));
        enhet.setEnhetsnamn(safeTextGet(jsonData, "enhetName"));
//        enhet.setEpost(jsonData.get("").textValue());
//        enhet.setPostadress(jsonData.get("").textValue());
//        enhet.setPostnummer(jsonData.get("").textValue());
//        enhet.setPostort(jsonData.get("").textValue());
//        enhet.setTelefonnummer(jsonData.get("").textValue());

        Vardgivare vardGivare = new Vardgivare();
//        vardGivare.setVardgivareId(InternalConverterUtil.getHsaId(jsonData.get("vardGivareId").textValue()));
        vardGivare.setVardgivarnamn(safeTextGet(jsonData, "vardGivareNamn"));
        enhet.setVardgivare(vardGivare);
        return enhet;
    }

    private static String safeTextGet(JsonNode data, String key) {
        if (data == null) {
            return null;
        }

        JsonNode tmp = data.get(key);
        if (tmp == null) {
            return null;
        }

        return tmp.textValue();
    }


    private static LocalDate safeDateGet(JsonNode data, String key) {
        if (data == null) {
            return null;
        }

        JsonNode tmp = data.get(key);
        if (tmp == null) {
            return null;
        }

        String dateString = tmp.textValue();
        if (dateString.isEmpty()) {
            return null;
        }

        return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private static LocalDateTime safeDateTimeGet(JsonNode data, String key) {
        if (data == null) {
            return null;
        }

        JsonNode tmp = data.get(key);
        if (tmp == null) {
            return null;
        }

        String dateString = tmp.textValue();
        if (dateString.isEmpty()) {
            return null;
        }

        if (dateString.endsWith("Z")) {
            dateString = dateString.replaceFirst(".$", "");
        }

        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static String toXml(SendMessageToRecipientType request) {
        var requestElement = new se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.ObjectFactory()
            .createSendMessageToRecipient(request);
        return XmlMarshallerHelper.marshal(requestElement);
    }

    public static String toXml(SendMessageToCareType request) {
        var requestElement = new se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.ObjectFactory()
            .createSendMessageToCare(request);
        return XmlMarshallerHelper.marshal(requestElement);
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
