package se.inera.intyg.intygstjanst.web.integrationtest;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;

public class ClasspathResourceResolver implements LSResourceResolver {
    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        try {
            return new DOMInputImpl(publicId, systemId, baseURI, load(baseURI, systemId), null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream load(String baseURI, String name) throws IOException {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (resourceAsStream == null) {
            String localName = name.replaceAll("^((\\.)+/)+", "");
            resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(localName);
        }
        return resourceAsStream;
    }

}