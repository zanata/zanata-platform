package org.jboss.shotoku.tags.dal;

import javax.persistence.*;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
@Entity
@Table(name = "ShotokuTagsVisitsIps")
public class VisitsIpsEntity {
    private VisitsIpsIdEntity viie;

    public VisitsIpsEntity(VisitsIpsIdEntity viie) {
        this.viie = viie;
    }

    public VisitsIpsEntity(String data, String type, String ip) {
        this(new VisitsIpsIdEntity(data, type, ip));
    }

    public VisitsIpsEntity() {

    }

    @EmbeddedId
    public VisitsIpsIdEntity getViie() {
        return viie;
    }

    public void setViie(VisitsIpsIdEntity viie) {
        this.viie = viie;
    }

    @Transient
    public String getData() {
        return viie.getData();
    }

    @Transient
    public String getType() {
        return viie.getType();
    }

    @Transient
    public String getIp() {
        return viie.getIp();
    }
}
