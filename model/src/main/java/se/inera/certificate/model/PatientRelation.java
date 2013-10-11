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

import java.util.ArrayList;
import java.util.List;

public class PatientRelation {

    private Kod relationskategori;
    private List<Kod> relationtyper;
    private Id personId;
    private List<String> fornamn;
    private String efternamn;
    private List<String> mellannamn;
    private String postadress;
    private String postnummer;
    private String postort;

    public Kod getRelationskategori() {
        return relationskategori;
    }

    public void setRelationskategori(Kod relationskategori) {
        this.relationskategori = relationskategori;
    }

    public List<Kod> getRelationtyper() {
        if (relationtyper == null) {
            relationtyper = new ArrayList<Kod>();
        }
        return this.relationtyper;
    }

    public Id getPersonId() {
        return personId;
    }

    public void setPersonId(Id personId) {
        this.personId = personId;
    }

    public List<String> getFornamn() {
        if (fornamn == null) {
            fornamn = new ArrayList<String>();
        }
        return this.fornamn;
    }

    public String getEfternamn() {
        return efternamn;
    }

    public void setEfternamn(String efternamn) {
        this.efternamn = efternamn;
    }

    public List<String> getMellannamn() {
        if (mellannamn == null) {
            mellannamn = new ArrayList<String>();
        }
        return this.mellannamn;
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

}
