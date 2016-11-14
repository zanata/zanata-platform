package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public final class PersonId implements IsSerializable {
    private String id;

    // for GWT
    @SuppressWarnings("unused")
    private PersonId() {
    }

    public PersonId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("Invalid Id");
        }
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof PersonId) {
            return ((PersonId) obj).id.equals(id);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static PersonId of(String id) {
        return new PersonId(id);
    }
}
