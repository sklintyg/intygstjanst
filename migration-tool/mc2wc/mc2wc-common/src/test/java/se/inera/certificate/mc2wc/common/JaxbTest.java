package se.inera.certificate.mc2wc.common;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import se.inera.certificate.mc2wc.message.MigrationMessage;

public class JaxbTest {

    public JaxbTest() {
        
    }

    @Test
    public void testUnmarshall() throws Exception {
        
        JAXBContext ctx = JAXBContext.newInstance(MigrationMessage.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        
        InputStream is = this.getClass().getResourceAsStream("/mc2wc-test-1.xml");
        MigrationMessage msg = (MigrationMessage) unmarshaller.unmarshal(is);
        
        assertNotNull(msg);
    }
    
}