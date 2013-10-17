package se.inera.certificate.migration.testutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.format.datetime.DateFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import se.inera.certificate.migration.testutils.dao.Cert;
import se.inera.certificate.migration.testutils.dao.CertTestDao;

/**
 * Utility for generating test data and inserting it into
 * a database.
 *  
 * @author nikpet
 *
 */
public class CertificateDataInitialiser {

    private static final String[] CERT_TEMPLATES = {"/data/legacy-maximalt-fk7263-transport.xml", "data/scenarios/legacy/scenario1.xml", "data/scenarios/legacy/scenario11.xml"};
    
    private static Logger LOG = LoggerFactory.getLogger(CertificateDataInitialiser.class);
    
    private DocumentBuilder docBuilder;
    
    private XPathExpression utlatandeIdXPath;
    
    private XPathExpression civicRegNbrXPath;
    
    private long timeFrom;
    
    private long timeRange;
    
    private boolean generateTestData = false;
    
    @Autowired
    private CertTestDao certTestDao;
        
    /**
     * Generate test certificates and insert into database.
     * 
     * @param nbrOfCerts Number of certificates to generate.
     * @throws Exception
     */
    public void generateAndLoadCerts(int nbrOfCerts) throws Exception {
        
        if (!generateTestData) {
            LOG.info("The generateTestData flag is set to false, generation of test data will not be done!");
            return;
        }
        
        LOG.info("Generating and loading {} certificates!", nbrOfCerts);
        
        for (int certs = 0; certs < nbrOfCerts; certs++) {
            Cert cert = generateCert();
            loadCert(cert);
        }
        
    }
    
    /**
     * Generates a Cert with a random Personnummer, a random certificate template and
     * a random signing date.
     * 
     * @return
     */
    private Cert generateCert() {
        String certId = UUID.randomUUID().toString();
        String civicRegNbr = PersonnummerGenerator.generateRandomPersonnummer();
        String template = getRandomCertificateTemplate();
        Cert cert = new Cert(certId, civicRegNbr, template);
        cert.setSignedDate(getRandomSignedDate());
        
        return cert;
    }
        
    private String getRandomCertificateTemplate() {
                
        Random rnd = new Random();
        
        if (CERT_TEMPLATES.length == 1) {
            return CERT_TEMPLATES[0];
        }
        
        int rndPos = rnd.nextInt(CERT_TEMPLATES.length - 1);
        
        return CERT_TEMPLATES[rndPos];
    }

    private DateTime getRandomSignedDate() {
        
        Random rnd = new Random(DateTime.now().getMillis());
        
        long randomDateTime = timeFrom + (long) (rnd.nextDouble() * timeRange);
                
        return new DateTime(randomDateTime);
    }

    public void loadCert(Cert cert) throws Exception {
        
        LOG.debug("Loading Cert: {}", cert.toString());
        
        byte[] certXML = readAndUpdateCertificateXML(cert);
        
        certTestDao.insertCert(cert);
        
        certTestDao.insertOriginalCertificate(cert.getCertId(), certXML);
    }
        
    public byte[] readAndUpdateCertificateXML(Cert cert) throws Exception {
        
        byte[] certXML = readCertificateTemplateFromFile(cert.getCertTemplate());
        
        ByteArrayInputStream is = new ByteArrayInputStream(certXML);
        
        Document document = docBuilder.parse(is);
        
        Node utlatandeIdNodeset = (Node) utlatandeIdXPath.evaluate(document, XPathConstants.NODE);
        utlatandeIdNodeset.setTextContent(cert.getCertId());
        
        Node civicRegNbrNodeset = (Node) civicRegNbrXPath.evaluate(document, XPathConstants.NODE);
        civicRegNbrNodeset.setTextContent(cert.getCivicRegNbr());
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(bos);
        
        transformer.transform(source, result);
        
        return bos.toByteArray();
    }
    
    private byte[] readCertificateTemplateFromFile(String filePath) {
        LOG.debug("Reading certificate from file '{}'", filePath);
        Resource fileRes = new ClassPathResource(filePath);
        
        try {
            return FileUtils.readFileToByteArray(fileRes.getFile());
        } catch (IOException e) {
            LOG.error("Can not read certificate from resource {}", filePath);
            return null;
        } 
        
    }
    
    public void init() throws Exception {
        
        calcYearOffsets();
        
        LOG.debug("Setting up DOM document builder");
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        this.docBuilder = dbf.newDocumentBuilder();
        
        LOG.debug("Setting up XPath expressions");
        
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        
        this.utlatandeIdXPath = xPath.compile("//RegisterMedicalCertificate/lakarutlatande/lakarutlatande-id");
        
        this.civicRegNbrXPath = xPath.compile("//RegisterMedicalCertificate/lakarutlatande/patient/person-id/@extension");
    }

    private void calcYearOffsets() {
        
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        
        this.timeFrom = dateTimeFormatter.parseDateTime("2013-01-01").getMillis();
        
        this.timeRange = dateTimeFormatter.parseDateTime("2013-12-31").getMillis() - this.timeFrom;
    }

    public boolean isGenerateTestData() {
        return generateTestData;
    }

    public void setGenerateTestData(boolean generateTestData) {
        this.generateTestData = generateTestData;
    }
}
