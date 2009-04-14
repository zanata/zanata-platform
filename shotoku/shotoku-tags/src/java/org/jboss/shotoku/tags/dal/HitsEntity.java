package org.jboss.shotoku.tags.dal;

import javax.persistence.*;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
@Entity
@Table(name = "ShotokuTagsHits")
public class HitsEntity {
    private HitsIdEntity id;
    private int count;

    public HitsEntity() {

    }

    public HitsEntity(HitsIdEntity id, int count) {
        this.id = id;
        this.count = count;
    }

    @EmbeddedId
    public HitsIdEntity getId() {
        return id;
    }

    public void setId(HitsIdEntity id) {
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
