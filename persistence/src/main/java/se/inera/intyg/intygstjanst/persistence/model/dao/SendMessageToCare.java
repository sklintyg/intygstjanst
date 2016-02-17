/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.intygstjanst.persistence.model.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FRAGASVAR")
public class SendMessageToCare {

    /**
     * The (system-wide) unique id for this entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long internReferens;

    @Column(name = "INTYGS_ID")
    private String intygsId;

    @Column(name = "MEDDELANDE_ID")
    private String meddelandeId;

    @Column(name = "MEDDELANDE")
    private String meddelande;

    // @Column(name = "REFERENS")
    // private String referens;

    // @Column(name = "FRAGE_SIGNERINGS_DATUM")
    // @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    // private LocalDateTime frageSigneringsDatum;

    public Long getInternReferens() {
        return internReferens;
    }

    public void setInternReferens(Long internReferens) {
        this.internReferens = internReferens;
    }

    // public LocalDateTime getFrageSigneringsDatum() {
    // return frageSigneringsDatum;
    // }
    //
    // public void setFrageSigneringsDatum(LocalDateTime frageSigneringsDatum) {
    // this.frageSigneringsDatum = frageSigneringsDatum;
    // }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            SendMessageToCare fragaSvar = (SendMessageToCare) o;

            if (internReferens == null) {
                return fragaSvar.internReferens == null;
            } else {
                return internReferens.equals(fragaSvar.internReferens);
            }
        }
    }

    @Override
    public int hashCode() {
        return internReferens != null ? internReferens.hashCode() : 0;
    }

    public String getIntygsId() {
        return intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public String getMeddelandeId() {
        return meddelandeId;
    }

    public void setMeddelandeId(String meddelandeId) {
        this.meddelandeId = meddelandeId;
    }

    public String getMeddelande() {
        return meddelande;
    }

    public void setMeddelande(String meddelande) {
        this.meddelande = meddelande;
    }

}
