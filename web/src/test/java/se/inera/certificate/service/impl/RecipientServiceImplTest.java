package se.inera.certificate.service.impl;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import se.inera.certificate.exception.RecipientUnknownException;
import se.inera.certificate.modules.support.api.dto.TransportModelVersion;
import se.inera.certificate.service.recipientservice.CertificateType;
import se.inera.certificate.service.recipientservice.Recipient;

public class RecipientServiceImplTest {

    RecipientServiceImpl service;

    @Before
    public void setUp() throws Exception {
        service = new RecipientServiceImpl();
        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("recipient.properties"));
        service.setRecipients(properties);
        service.afterPropertiesSet();
    }

    @Test
    public void testListRecipients() {
        assertTrue("Got null!", service.listRecipients().size() > 0);
    }
    
    @Test
    public void testListRecipientsForCerttypeFK7263() {
        List<Recipient> expected = Arrays.asList(new Recipient("FK", "Försäkringskassan", "fk"));
        
        assertEquals(expected, service.listRecipients(new CertificateType("fk7263")));
    }

    @Test
    public void testListRecipientsForCerttypeTS() {
        List<Recipient> expected = Arrays.asList(new Recipient("TS", "Transportstyrelsen", "ts"));
        
        assertEquals(expected, service.listRecipients(new CertificateType("ts-bas")));
        assertEquals(expected, service.listRecipients(new CertificateType("ts-diabetes")));
    }
    
    @Test
    public void testGetTransportModelVersionForFK7263() throws RecipientUnknownException {
        assertEquals(TransportModelVersion.LEGACY_LAKARUTLATANDE, service.getVersion("FK", "fk7263"));
    }

    @Test
    public void testGetTransportModelVersionForTsBas() throws RecipientUnknownException {
        assertEquals(TransportModelVersion.UTLATANDE_V1, service.getVersion("TS", "ts-bas"));
    }
    
    @Test(expected = RecipientUnknownException.class)
    public void testUnknownRecipient() throws RecipientUnknownException {
        service.getVersion("F K", "fk7263");
    }
}
