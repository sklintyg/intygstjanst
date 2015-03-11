package se.inera.certificate.service.builder;

import liquibase.util.StringUtils;
import se.inera.certificate.service.bean.Recipient;

import java.util.List;

/**
 * Recipient object.
 *
 * @author erik
 *
 */
public class RecipientBuilder {

    private String logicalAddress;
    private String name;
    private String id;
    private String certificateTypes;

    public Recipient build() {
        return new Recipient(logicalAddress, name, id, certificateTypes);
    }

    public String getLogicalAddress() {
        return logicalAddress;
    }

    public RecipientBuilder setLogicalAddress(String logicalAddress) {
        this.logicalAddress = logicalAddress;
        return this;
    }

    public String getName() {
        return name;
    }

    public RecipientBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public String getId() {
        return id;
    }

    public RecipientBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public String getCertificateTypes() {
        return certificateTypes;
    }

    public void setCertificateTypes(String certificateTypes) {
        this.certificateTypes = certificateTypes;
    }

    public void setCertificateTypes(List<String> certificateTypes) {
        this.certificateTypes = StringUtils.join(certificateTypes, Recipient.SEPARATOR);
    }

}
