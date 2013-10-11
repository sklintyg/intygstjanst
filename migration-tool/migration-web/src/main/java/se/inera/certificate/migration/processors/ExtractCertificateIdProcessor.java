package se.inera.certificate.migration.processors;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import se.inera.certificate.migration.model.OriginalCertificate;


public class ExtractCertificateIdProcessor implements ItemProcessor<OriginalCertificate, OriginalCertificate> {

    private static Logger log = LoggerFactory.getLogger(ExtractCertificateIdProcessor.class);
    
    private DocumentBuilder docBuilder;
    
    private XPathExpression xPathExpression;
    
    public OriginalCertificate process(OriginalCertificate cert) throws Exception {
        
        log.debug("Extracting id from certificate");
        
        InputSource is = new InputSource(new StringReader(cert.getOrignalCertificateAsString())); 
        
        Document document = docBuilder.parse(is);
        
        String res = (String) xPathExpression.evaluate(document, XPathConstants.STRING);
        
        if(res == null) {
            log.error("Extraction of id failed original certificate with id {}", cert.getOriginalCertificateId());
        }
        
        cert.setCertificateId(res);
        
        return cert;
    }
    
    public void init() throws Exception {
        
        log.debug("Setting up DOM document builder");
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        this.docBuilder = dbf.newDocumentBuilder();
        
        log.debug("Setting up XPath expression");
        
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        
        this.xPathExpression = xPath.compile("//utlatande/utlatande-id/@extension");
        
    }
    
}
