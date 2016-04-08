package se.inera.intyg.intygstjanst.web.integrationtest.arende;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.base.Throwables;

public class XPathExtractor {
    private XPath xpath;
    private Document xmlDocument;

    public XPathExtractor(final String message, final Map<String, String> namespaceMap) {
        InputSource inputSource = new InputSource(new StringReader(message));
        setup(inputSource, namespaceMap);
    }

    private XPathFactory getXPathFactory() {
        return XPathFactory.newInstance();
    }

    private void setup(InputSource inputSource, Map<String, String> namespaceMap) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            xmlDocument = domFactory.newDocumentBuilder().parse(inputSource);

            xpath = getXPathFactory().newXPath();
            xpath.setNamespaceContext(new XPathNamespaceContext(namespaceMap));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public String getFragmentFromXPath(String xPathExpression) {
        try {
            XPathExpression expr = xpath.compile(xPathExpression);
            NodeList matches = (NodeList) expr.evaluate(xmlDocument, XPathConstants.NODESET);
            if (matches.getLength() > 0) {
                Node node = matches.item(0);
                StringWriter writer = new StringWriter();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.transform(new DOMSource(node), new StreamResult(writer));
                return writer.toString();
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return null;
    }

    private static class XPathNamespaceContext implements NamespaceContext {

        private final Map<String, String> namespaceMap;

        public XPathNamespaceContext(final Map<String, String> namespaceMap) {
            this.namespaceMap = namespaceMap;
        }

        public String getNamespaceURI(final String prefix) {
            if (namespaceMap.get(prefix) != null) {
                return namespaceMap.get(prefix);
            }
            return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(final String uri) {
            throw new UnsupportedOperationException();
        }

        public Iterator<String> getPrefixes(final String uri) {
            throw new UnsupportedOperationException();
        }

    }

}
