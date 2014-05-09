package se.inera.certificate.service.recipientservice;

/**
 * Recipient object
 * 
 * @author erik
 * 
 */
public class RecipientBuilder {

    private String logicalAddress;

    private String name;

    private String id;

    public Recipient build() {
        return new Recipient(logicalAddress, name, id);
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
}
