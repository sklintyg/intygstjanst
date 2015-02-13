package se.inera.certificate.service.impl;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import se.inera.certificate.exception.RecipientUnknownException;
import se.inera.certificate.modules.support.api.dto.TransportModelVersion;
import se.inera.certificate.service.recipientservice.CertificateType;
import se.inera.certificate.service.recipientservice.Recipient;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class RecipientServiceImplTest {

    RecipientServiceImpl service;

    private static final String FK_CERTIFICATE_TYPE = "fk7263";
    private static final String TS_CERTIFICATE_TYPE_BAS = "ts-bas";
    private static final String TS_CERTIFICATE_TYPE_DIABETES = "ts-diabetes";

    private static final String FK_RECIPIENT_ID = "FK";
    private static final String FK_RECIPIENT_NAME = "Försäkringskassan";
    private static final String FK_RECIPIENT_LOGICALADDRESS = "FKORG";
    private static final String FK_RECIPIENT_CERTIFICATETYPES = "fk7263";

    private static final String TS_RECIPIENT_ID = "TS";
    private static final String TS_RECIPIENT_NAME = "Transportstyrelsen";
    private static final String TS_RECIPIENT_LOGICALADDRESS = "tsTestAddress";
    private static final String TS_RECIPIENT_CERTIFICATETYPES = "ts-bas,ts-diabetes";

    private Recipient createFkRecipient() {
        return new Recipient(FK_RECIPIENT_LOGICALADDRESS,
                FK_RECIPIENT_NAME,
                FK_RECIPIENT_ID,
                FK_RECIPIENT_CERTIFICATETYPES);

    }

    private Recipient createTsRecipient() {
        return new Recipient(TS_RECIPIENT_LOGICALADDRESS,
                TS_RECIPIENT_NAME,
                TS_RECIPIENT_ID,
                TS_RECIPIENT_CERTIFICATETYPES);

    }

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("recipient.properties"));

        service = new RecipientServiceImpl();
        service.setRecipients(properties);
        service.afterPropertiesSet();
    }

    @Test
    public void testListRecipients() {
        assertTrue("Got null!", service.listRecipients().size() > 0);
    }
    
    @Test
    public void testListRecipientsForCerttypeFK7263() throws RecipientUnknownException {
        List<Recipient> expected = Arrays.asList(createFkRecipient());
        
        assertEquals(expected, service.listRecipients(new CertificateType(FK_CERTIFICATE_TYPE)));
    }

    @Test
    public void testListRecipientsForCerttypeTS() throws RecipientUnknownException {
        List<Recipient> expected = Arrays.asList(createTsRecipient());
        
        assertEquals(expected, service.listRecipients(new CertificateType(TS_CERTIFICATE_TYPE_BAS)));
        assertEquals(expected, service.listRecipients(new CertificateType(TS_CERTIFICATE_TYPE_DIABETES)));
    }
    
    @Test
    public void testGetTransportModelVersionForFK7263() throws RecipientUnknownException {
        assertEquals(TransportModelVersion.LEGACY_LAKARUTLATANDE, service.getVersion("FKORG", "fk7263"));
    }

    @Test
    public void testGetTransportModelVersionForTsBas() throws RecipientUnknownException {
        assertEquals(TransportModelVersion.UTLATANDE_V1, service.getVersion("tsTestAddress", "ts-bas"));
    }
    
    @Test(expected = RecipientUnknownException.class)
    public void testUnknownRecipient() throws RecipientUnknownException {
        service.getVersion("F K", "fk7263");
    }
}
