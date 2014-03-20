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

    public final Kod getRelationskategori() {
        return relationskategori;
    }

    public final void setRelationskategori(Kod relationskategori) {
        this.relationskategori = relationskategori;
    }

    public final List<Kod> getRelationtyper() {
        if (relationtyper == null) {
            relationtyper = new ArrayList<Kod>();
        }
        return this.relationtyper;
    }

    public final Id getPersonId() {
        return personId;
    }

    public final void setPersonId(Id personId) {
        this.personId = personId;
    }

    public final List<String> getFornamn() {
        if (fornamn == null) {
            fornamn = new ArrayList<String>();
        }
        return this.fornamn;
    }

    public final String getEfternamn() {
        return efternamn;
    }

    public final void setEfternamn(String efternamn) {
        this.efternamn = efternamn;
    }

    public final List<String> getMellannamn() {
        if (mellannamn == null) {
            mellannamn = new ArrayList<String>();
        }
        return this.mellannamn;
    }

    public final String getPostadress() {
        return postadress;
    }

    public final void setPostadress(String postadress) {
        this.postadress = postadress;
    }

    public final String getPostnummer() {
        return postnummer;
    }

    public final void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    public final String getPostort() {
        return postort;
    }

    public final void setPostort(String postort) {
        this.postort = postort;
    }
}
