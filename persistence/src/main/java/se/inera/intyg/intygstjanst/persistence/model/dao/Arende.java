/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import java.time.LocalDateTime;

import javax.persistence.*;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "ARENDE")
public class Arende {

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

    @Column(name = "MEDDELANDE_DATA")
    private String meddelande;

    @Column(name = "REFERENS_ID")
    private String referens;

    @Column(name = "AMNE")
    private String amne;

    @Column(name = "LOGISK_ADRESSMOTTAGARE")
    private String logiskAdressmottagare;

    @Column(name = "TIMESTAMP")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime timestamp;

    public Long getInternReferens() {
        return internReferens;
    }

    public void setInternReferens(Long internReferens) {
        this.internReferens = internReferens;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimeStamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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

    public String getReferens() {
        return referens;
    }

    public void setReferens(String referens) {
        this.referens = referens;
    }

    public void setLogiskAdressmottagare(String logiskAdressmottagare) {
        this.logiskAdressmottagare = logiskAdressmottagare;
    }

    public String getLogiskAdressmottagare() {
        return logiskAdressmottagare;
    }

    public String getAmne() {
        return amne;
    }

    public void setAmne(String amne) {
        this.amne = amne;
    }

    @Override
    public int hashCode() {
        return internReferens != null ? internReferens.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            Arende fragaSvar = (Arende) o;

            if (internReferens == null) {
                return fragaSvar.internReferens == null;
            } else {
                return internReferens.equals(fragaSvar.internReferens);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append("Intern referens: ").append(this.internReferens).append(" \n").append("INTYGS_ID: ")
                .append(this.intygsId)
                .append(" \n").append("MEDDELANDE_ID: ").append(this.meddelandeId).append(" \n").append("REFERENS_ID: ")
                .append(this.referens)
                .append(" \n").append("AMNE: ").append(this.amne).append(" \n").append("LOGISK_ADRESSMOTTAGARE: ")
                .append(this.logiskAdressmottagare)
                .append(" \n").append("TIMESTAMP: ").append(this.timestamp).toString();
    }

}
