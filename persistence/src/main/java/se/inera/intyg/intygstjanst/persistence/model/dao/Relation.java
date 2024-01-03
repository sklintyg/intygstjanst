/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * Relation entity. Forms an association between two Certificates of a given type.
 *
 * Created by eriklupander on 2017-05-09.
 */
@Entity
@Table(name = "RELATION")
public class Relation {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "FROM_INTYG_ID", nullable = false)
    private String fromIntygsId;

    @Column(name = "TO_INTYG_ID", nullable = false)
    private String toIntygsId;

    @Column(name = "RELATION_KOD", nullable = false)
    private String relationKod;

    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime created;

    public Relation() {
    }

    public Relation(String fromIntygsId, String toIntygsId, String relationKod, LocalDateTime created) {
        this.fromIntygsId = fromIntygsId;
        this.toIntygsId = toIntygsId;
        this.relationKod = relationKod;
        this.created = created;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFromIntygsId() {
        return fromIntygsId;
    }

    public void setFromIntygsId(String fromIntygsId) {
        this.fromIntygsId = fromIntygsId;
    }

    public String getToIntygsId() {
        return toIntygsId;
    }

    public void setToIntygsId(String toIntygsId) {
        this.toIntygsId = toIntygsId;
    }

    public String getRelationKod() {
        return relationKod;
    }

    public void setRelationKod(String relationKod) {
        this.relationKod = relationKod;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
