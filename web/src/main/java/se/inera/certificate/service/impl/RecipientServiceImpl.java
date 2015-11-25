package se.inera.certificate.service.impl;

import org.springframework.beans.factory.InitializingBean;
import se.inera.certificate.exception.RecipientUnknownException;
import se.inera.intyg.common.support.modules.support.api.dto.TransportModelVersion;
import se.inera.certificate.service.RecipientService;
import se.inera.certificate.service.bean.CertificateType;
import se.inera.certificate.service.bean.Recipient;
import se.inera.certificate.service.builder.RecipientBuilder;
import se.inera.certificate.service.bean.RecipientCertificateType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class RecipientServiceImpl implements RecipientService, InitializingBean {

    private List<Recipient> recipientList;
    private Map<Recipient, Set<CertificateType>> certificateTypesForRecipient;
    private Properties recipients;

    /**
     * Keeps track of the TransportModelVersion supported by a certain combination of Logical Address and
     * CertificateType.
     */
    private Map<RecipientCertificateType, TransportModelVersion> supportedTransportModelVersion;

    public RecipientServiceImpl() {
        recipientList = new ArrayList<>();
        certificateTypesForRecipient = new HashMap<>();
        supportedTransportModelVersion = new HashMap<>();
    }

    public Properties getRecipients() {
        return recipients;
    }

    public void setRecipients(Properties recipients) {
        this.recipients = recipients;
    }

    @Override
    public Recipient getRecipientForLogicalAddress(String logicalAddress) throws RecipientUnknownException {
        for (Recipient r : recipientList) {
            if (r.getLogicalAddress().equals(logicalAddress)) {
                return r;
            }
        }
        throw new RecipientUnknownException(String.format("No recipient found for logical address: %s", logicalAddress));
    }

    @Override
    public Recipient getRecipient(String recipientId) throws RecipientUnknownException {
        for (Recipient r : recipientList) {
            if (r.getId().equalsIgnoreCase(recipientId)) {
                return r;
            }
        }
        throw new RecipientUnknownException(String.format("No recipient found for recipient id: %s", recipientId));
    }

    @Override
    public List<Recipient> listRecipients() {
        return recipientList;
    }

    @Override
    public List<Recipient> listRecipients(CertificateType certificateType) throws RecipientUnknownException {
        List<Recipient> list = new ArrayList<Recipient>();

        try {
            for (Recipient r : recipientList) {
                if (r.getCertificateTypes().contains(certificateType.getCertificateTypeId())) {
                    list.add(r);
                }
            }
        } catch (Exception e) {
            throw new RecipientUnknownException(String.format("No recipient found for certificate type: %s", certificateType.getCertificateTypeId()));
        }

        return list;
    }

    @Override
    public Set<CertificateType> listCertificateTypes(Recipient recipient) {
        return certificateTypesForRecipient.get(recipient);
    }

    @Override
    public TransportModelVersion getVersion(String logicalAddress, String certificateType) throws RecipientUnknownException {
        String recipientId = getRecipientForLogicalAddress(logicalAddress).getId();
        return supportedTransportModelVersion.get((new RecipientCertificateType(recipientId, certificateType)));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        HashMap<String, RecipientBuilder> recipientMap = new HashMap<>();

        for (String key : recipients.stringPropertyNames()) {
            String value = recipients.getProperty(key);
            String[] keyParts = key.split("\\.");
            switch (keyParts[0]) {
            case "recipient":
                String id = keyParts[1];
                if (recipientMap.get(id) == null) {
                    recipientMap.put(id, new RecipientBuilder().setId(id));
                }
                if (keyParts[2].equals("name")) {
                    recipientMap.get(id).setName(value);
                } else if (keyParts[2].equals("logicalAddress")) {
                    recipientMap.get(id).setLogicalAddress(value);
                } else if (keyParts[2].equals("certificateType")) {
                    recipientMap.get(id).setCertificateTypes(value);
                }
                break;
            case "recipient-transport-model-version":
                String recipientId = keyParts[1];
                String certType = keyParts[2];
                supportedTransportModelVersion.put(new RecipientCertificateType(recipientId, certType),
                        TransportModelVersion.valueOf(value));
                break;
            default:
            }
        }

        for (RecipientBuilder builder : recipientMap.values()) {
            recipientList.add(builder.build());
        }
    }
}
