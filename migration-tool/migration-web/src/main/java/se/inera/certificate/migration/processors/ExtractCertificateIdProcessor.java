package se.inera.certificate.migration.processors;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import se.inera.certificate.migration.model.OriginalCertificate;


public class ExtractCertificateIdProcessor implements ItemProcessor<OriginalCertificate, OriginalCertificate> {

    private static Logger log = LoggerFactory.getLogger(ExtractCertificateIdProcessor.class);
    
    private DocumentBuilder docBuilder;
    
    private XPathExpression xPathExpression;
    
    public OriginalCertificate process(OriginalCertificate orgCert) throws Exception {
        
        log.debug("Extracting certificate id from original certificate XML with id {0}", orgCert.getOriginalCertificateId());
        
        String certificateId = extractIdFromOriginalXML(orgCert);
        
        if (StringUtils.isEmpty(certificateId)) {
            String errMsg = MessageFormat.format("Extraction of certificate id from OriginalCertificate failed: {0}", orgCert.toString());
            throw new CertificateProcessingException(errMsg);
        }
        
        orgCert.setCertificateId(certificateId);
        
        return orgCert;
    }
    
    private String extractIdFromOriginalXML(OriginalCertificate orgCert) throws CertificateProcessingException {
        
        Document document = parseXMLToDocument(orgCert);
        
        try {
            String res = (String) xPathExpression.evaluate(document, XPathConstants.STRING);
            return res;
        } catch (XPathExpressionException e) {
            String errMsg = MessageFormat.format("XPath evaluation on orignal XML with id {0} failed", orgCert.getOriginalCertificateId());
            throw new CertificateProcessingException(errMsg, e);
        }
    }
    
    private Document parseXMLToDocument(OriginalCertificate orgCert) throws CertificateProcessingException {
        
        InputSource is = new InputSource(new StringReader(orgCert.getOrignalCertificateAsString())); 
        
        try {
            return docBuilder.parse(is);
        } catch (SAXException | IOException e) {
            String errMsg = MessageFormat.format("An error occured when parsing original XML with id {0}", orgCert.getOriginalCertificateId());
            throw new CertificateProcessingException(errMsg, e);
        }   
    }
    
    public void init() throws Exception {
        
        log.debug("Setting up DOM document builder");
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        this.docBuilder = dbf.newDocumentBuilder();
        
        log.debug("Setting up XPath expression");
        
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        
        this.xPathExpression = xPath.compile("//RegisterMedicalCertificate/lakarutlatande/lakarutlatande-id");
        
    }
    
}
