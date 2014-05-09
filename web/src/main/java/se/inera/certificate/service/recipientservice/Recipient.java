package se.inera.certificate.service.recipientservice;

import static org.springframework.util.Assert.hasText;

/**
 * Recipient object
 * 
 * @author erik
 * 
 */
public class Recipient {

    private final String logicalAddress;

    private final String name;

    private final String id;

    public Recipient(String logicalAddress, String name, String id) {
        hasText(logicalAddress, "Logical address must not be empty");
        hasText(name, "Name must not be empty");
        hasText(id, "Id must not be empty");

        this.logicalAddress = logicalAddress;
        this.name = name;
        this.id = id;
    }

    public String getLogicalAddress() {
        return logicalAddress;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Logical address: " + logicalAddress + " name: " + name + " id: " + id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((logicalAddress == null) ? 0 : logicalAddress.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Recipient other = (Recipient) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (logicalAddress == null) {
            if (other.logicalAddress != null)
                return false;
        } else if (!logicalAddress.equals(other.logicalAddress))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
