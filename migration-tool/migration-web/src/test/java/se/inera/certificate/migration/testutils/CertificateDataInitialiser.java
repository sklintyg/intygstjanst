package se.inera.certificate.migration.testutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import se.inera.certificate.migration.testutils.dao.Cert;
import se.inera.certificate.migration.testutils.dao.CertTestDao;

public class CertificateDataInitialiser {

    private static Logger LOG = LoggerFactory.getLogger(CertificateDataInitialiser.class);
    
    private DocumentBuilder docBuilder;
    
    private XPathExpression utlatandeIdXPath;
    
    private XPathExpression civicRegNbrXPath;
    
    private boolean generateTestData = false;
    
    @Autowired
    private CertTestDao certTestDao;
    
    public void loadCerts(List<Cert> certs, String originalCertificateFilePath) throws Exception {
        
        if (!generateTestData) {
            LOG.info("Generation of test data WILL NOT be done!");
            return;
        }
        
        LOG.info("Starting generation of test data, {} certificates will be inserted!", certs.size());
        
        byte[] orgCertXML = readOriginalCertificateFromFile(originalCertificateFilePath);
        
        for (Cert cert : certs) {
            loadCert(cert, orgCertXML);
        }
        
    }
        
    public void loadCert(Cert cert, byte[] orgCertXML) throws Exception {
        
        LOG.debug("Loading Cert with id {}", cert.getCertId());
        
        if (orgCertXML == null) {
            LOG.error("Can not complete insert operation for certificate with id {}", cert.getCertId());
            return;
        }
        
        byte[] updatedCertXML = updateCertificateXML(cert, orgCertXML);
        
        certTestDao.insertCert(cert);
        
        certTestDao.insertOriginalCertificate(cert.getCertId(), updatedCertXML);
    }
    
    public byte[] updateCertificateXML(Cert cert, byte[] certXML) throws Exception {
        
        LOG.debug("Updating cert XML with id {} and civic reg nbr {}", cert.getCertId(), cert.getCivicRegNbr());
        
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
    
    private byte[] readOriginalCertificateFromFile(String filePath) {
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
        
        LOG.debug("Setting up DOM document builder");
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        this.docBuilder = dbf.newDocumentBuilder();
        
        LOG.debug("Setting up XPath expressions");
        
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        
        this.utlatandeIdXPath = xPath.compile("//utlatande/utlatande-id/@extension");
        
        this.civicRegNbrXPath = xPath.compile("//utlatande/patient/person-id/@extension");
    }

    public boolean isGenerateTestData() {
        return generateTestData;
    }

    public void setGenerateTestData(boolean generateTestData) {
        this.generateTestData = generateTestData;
    }
}
