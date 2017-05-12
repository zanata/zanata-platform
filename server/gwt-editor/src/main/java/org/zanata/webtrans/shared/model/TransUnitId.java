package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class TransUnitId implements IsSerializable, Identifier<Long>,
        Serializable {
    private static final long serialVersionUID = 7666166109552526377L;
    private long id;

    // for GWT
    @SuppressWarnings("unused")
    private TransUnitId() {
    }

    public TransUnitId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public int hashCode() {
        return (int) id;
    }

    @Override
    public Long getValue() {
        return id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof TransUnitId) {
            return ((TransUnitId) obj).id == id;
        }
        return false;
    }

}
