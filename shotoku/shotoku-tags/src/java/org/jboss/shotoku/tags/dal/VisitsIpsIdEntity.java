package org.jboss.shotoku.tags.dal;

import org.jboss.shotoku.tools.Tools;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
@Embeddable
public class VisitsIpsIdEntity implements Serializable {
    private String data;
    private String type;
    private String ip;

    public VisitsIpsIdEntity(String data, String type, String ip) {
        this.data = data;
        this.type = type;
        this.ip = ip;
    }

    public VisitsIpsIdEntity() {

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Column(length = 32)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(length=15)
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int hashCode() {
        return (data == null ? 0 : data.hashCode()) +
                (type == null ? 0 : type.hashCode()) +
                (ip == null ? 0 : ip.hashCode());
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof VisitsIpsIdEntity)) return false;
        VisitsIpsIdEntity viie = (VisitsIpsIdEntity) obj;
        return Tools.objectsEqual(type, viie.type) &&
                Tools.objectsEqual(data, viie.data) &&
                Tools.objectsEqual(ip, viie.ip);
    }
}
