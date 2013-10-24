package se.inera.certificate.integration.util;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
        SOAPEnvelope env = soapMessage.getSOAPPart().getEnvelope();
        SOAPBodyElement body = (SOAPBodyElement) soapMessage.getSOAPBody().getChildElements(bodyName).next();
        return documentFromSoapBody(body, getNamespaceDeclarations(env, body));
    }

    public static Document documentFromSoapBody(SOAPMessage soapMessage) throws ParserConfigurationException,
            SOAPException {
        SOAPEnvelope env = soapMessage.getSOAPPart().getEnvelope();
        SOAPBodyElement body = (SOAPBodyElement) soapMessage.getSOAPBody().getChildNodes().item(0);
        return documentFromSoapBody(body, getNamespaceDeclarations(env, body));
    }

    private static HashMap<String, String> getNamespaceDeclarations(SOAPEnvelope env, SOAPBodyElement body) {
        HashMap<String, String> namespaceDeclarations = new HashMap<String, String>();
        @SuppressWarnings("rawtypes")
        Iterator nss = env.getNamespacePrefixes();
        // Retrieve all namespace declarations from SOAP-ENV node
        while (nss.hasNext()) {
            String prefix = (String) nss.next();
            String uri = env.getNamespaceURI(prefix);
            // filter out SOAP-ENV namespace, since it is not interesting to us
            if (!uri.startsWith("http://schemas.xmlsoap.org/soap/envelope")) {
                namespaceDeclarations.put(prefix, uri);
            }
        }
        // Retrieve all namespace declarations from SOAP-BODY node
        nss = body.getNamespacePrefixes();
        while (nss.hasNext()) {
            String prefix = (String) nss.next();
            String uri = env.getNamespaceURI(prefix);
            namespaceDeclarations.put(prefix, uri);
        }
        return namespaceDeclarations;
    }

    private static Document documentFromSoapBody(SOAPBodyElement element, HashMap<String, String> namespaceDeclarations) throws ParserConfigurationException {
        Document document = dbf.newDocumentBuilder().newDocument();
        Node node = document.importNode(element, true);
        document.appendChild(node);
        
        for (String prefix : namespaceDeclarations.keySet()) {
            String uri = namespaceDeclarations.get(prefix);
            if (node.lookupNamespaceURI(prefix) == null) {
                document.getDocumentElement().setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + prefix, uri);
            }
        }
        return document;
    }

    public static String getDocumentAsString(Document doc) throws TransformerException {
        StringWriter sw = new StringWriter();
        tf.newTransformer().transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}
