package org.jboss.shotoku.tags.dal;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.EmbeddedId;
import javax.persistence.Transient;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
@Entity
@Table(name = "ShotokuTagsVisits")
public class VisitsEntity {
    private VisitsIdEntity id;
    private int count;

    public VisitsEntity() {

    }

    public VisitsEntity(VisitsIdEntity id, int count) {
        this.id = id;
        this.count = count;
    }

    @EmbeddedId
    public VisitsIdEntity getId() {
        return id;
    }

    public void setId(VisitsIdEntity id) {
        this.id = id;
    }

    @Transient
    public String getType() {
        return id.getType();
    }

    @Transient
    public String getData() {
        return id.getData();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
