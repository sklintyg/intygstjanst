package se.inera.intyg.intygstjanst.persistence.model.dao;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

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
