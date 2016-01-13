package se.inera.certificate.model.dao;

import se.inera.certificate.modules.support.api.dto.Personnummer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author andreaskaltenbach
 */
@Entity
@Table(name = "CONSENT")
public class Consent {

    @javax.persistence.Id
    @Column(name = "CIVIC_REGISTRATION_NUMBER")
    private String civicRegistrationNumber;

    public Consent() {
    }

    public Consent(Personnummer civicRegistrationNumber) {
        this.civicRegistrationNumber = civicRegistrationNumber.getPersonnummer();
    }

    public Personnummer getCivicRegistrationNumber() {
        return new Personnummer(civicRegistrationNumber);
    }

    public void setCivicRegistrationNumber(Personnummer civicRegistrationNumber) {
        this.civicRegistrationNumber = civicRegistrationNumber.getPersonnummer();
    }
}
