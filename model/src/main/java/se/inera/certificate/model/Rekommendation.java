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

public class Rekommendation {

    private Kod rekommendationskod;

    private String beskrivning;

    private Kod sjukdomskannedom;

    public final Kod getRekommendationskod() {
        return rekommendationskod;
    }

    public final void setRekommendationskod(Kod rekommendationskod) {
        this.rekommendationskod = rekommendationskod;
    }

    public final String getBeskrivning() {
        return beskrivning;
    }

    public final void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

    public final Kod getSjukdomskannedom() {
        return sjukdomskannedom;
    }

    public final void setSjukdomskannedom(Kod sjukdomskannedom) {
        this.sjukdomskannedom = sjukdomskannedom;
    }
}
