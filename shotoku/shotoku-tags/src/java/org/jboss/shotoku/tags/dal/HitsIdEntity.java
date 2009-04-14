package org.jboss.shotoku.tags.dal;

import org.jboss.shotoku.tools.Tools;

import javax.persistence.Embeddable;
import javax.persistence.Column;
import java.io.Serializable;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
@Embeddable
public class HitsIdEntity implements Serializable {
    private String data;
    private String type;

    public HitsIdEntity() {

    }

    public HitsIdEntity(String data, String type) {
        this.data = data;
        this.type = type;
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

    public int hashCode() {
        return (data == null ? 0 : data.hashCode()) +
                (type == null ? 0 : type.hashCode());
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof HitsIdEntity)) return false;
        HitsIdEntity cie = (HitsIdEntity) obj;
        return Tools.objectsEqual(type, cie.type) &&
                Tools.objectsEqual(data, cie.data);
    }
}
