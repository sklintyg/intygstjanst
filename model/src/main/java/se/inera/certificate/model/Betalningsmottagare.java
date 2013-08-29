/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate Modules (http://code.google.com/p/inera-certificate-modules).
 *
 * Inera Certificate Modules is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Inera Certificate Modules is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.certificate.model;

public class Betalningsmottagare {

    private String namn;
    private String postadress;
    private String postnummer;
    private String postort;
    private Id organisationsnummer;
    private String plusgiroEllerBankgiro;
    private Boolean begarArvode;
    private String arvodeBegart;
    private Kod skattesedel;

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public String getPostadress() {
        return postadress;
    }

    public void setPostadress(String postadress) {
        this.postadress = postadress;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    public String getPostort() {
        return postort;
    }

    public void setPostort(String postort) {
        this.postort = postort;
    }

    public Id getOrganisationsnummer() {
        return organisationsnummer;
    }

    public void setOrganisationsnummer(Id organisationsnummer) {
        this.organisationsnummer = organisationsnummer;
    }

    public String getPlusgiroEllerBankgiro() {
        return plusgiroEllerBankgiro;
    }

    public void setPlusgiroEllerBankgiro(String plusgiroEllerBankgiro) {
        this.plusgiroEllerBankgiro = plusgiroEllerBankgiro;
    }

    public Boolean getBegarArvode() {
        return begarArvode;
    }

    public void setBegarArvode(Boolean begarArvode) {
        this.begarArvode = begarArvode;
    }

    public String getArvodeBegart() {
        return arvodeBegart;
    }

    public void setArvodeBegart(String arvodeBegart) {
        this.arvodeBegart = arvodeBegart;
    }

    public Kod getSkattesedel() {
        return skattesedel;
    }

    public void setSkattesedel(Kod skattesedel) {
        this.skattesedel = skattesedel;
    }
}
