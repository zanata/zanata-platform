package org.zanata.webtrans.shared.model;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.IsSerializable;

public class Person implements HasIdentifier<PersonId>, IsSerializable {
    private PersonId id;
    private String name;
    private String avatarUrl;

    // for GWT
    @SuppressWarnings("unused")
    private Person() {
    }

    public Person(PersonId id, String name, String avatarUrl) {
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(name, "name cannot be null");
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public PersonId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return Strings.nullToEmpty(avatarUrl);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((avatarUrl == null) ? 0 : avatarUrl.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Person other = (Person) obj;
        return Objects.equal(avatarUrl, other.avatarUrl)
                && Objects.equal(id, other.id);
    }
}
