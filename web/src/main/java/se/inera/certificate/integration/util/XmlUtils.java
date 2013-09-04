package se.inera.certificate.integration.util;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 */
public class XmlUtils {

    private static DocumentBuilderFactory dbf;
    private static TransformerFactory tf;

    static {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        tf = TransformerFactory.newInstance();
    }

    public static Document documentFromSoapBody(SOAPMessage soapMessage, QName bodyName)
            throws ParserConfigurationException, SOAPException {
        SOAPBodyElement e = (SOAPBodyElement) soapMessage.getSOAPBody().getChildElements(bodyName).next();
        return documentFromSoapBody(e);
    }

    public static Document documentFromSoapBody(SOAPMessage soapMessage) throws ParserConfigurationException,
            SOAPException {
        SOAPBodyElement e = (SOAPBodyElement) soapMessage.getSOAPBody().getChildNodes().item(0);
        return documentFromSoapBody(e);
    }

    private static Document documentFromSoapBody(SOAPBodyElement element) throws ParserConfigurationException {
        Document document = dbf.newDocumentBuilder().newDocument();
        Node node = document.importNode(element, true);
        document.appendChild(node);
        return document;
    }

    public static String getDocumentAsString(Document doc) throws TransformerException {
        StringWriter sw = new StringWriter();
        tf.newTransformer().transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}
