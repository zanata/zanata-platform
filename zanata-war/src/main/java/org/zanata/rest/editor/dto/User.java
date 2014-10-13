package org.zanata.rest.editor.dto;

import java.io.Serializable;
import java.lang.String;import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.Namespaces;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.MediaTypes.Format;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "username", "email", "name", "gravatarHash" })
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class User implements Serializable {

    private String username;

    private String email;

    private String name;

    private String gravatarHash;

    public User() {
        this(null, null, null, null);
    }

    public User(String username, String email, String name,
            String gravatarHash) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.gravatarHash = gravatarHash;
    }

    @Email
    @NotNull
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NotEmpty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotEmpty
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NotEmpty
    public String getGravatarHash() {
        return gravatarHash;
    }

    public void setGravatarHash(String gravatarHash) {
        this.gravatarHash = gravatarHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (email != null ? !email.equals(user.email) : user.email != null)
            return false;
        if (gravatarHash != null ? !gravatarHash.equals(user.gravatarHash) :
            user.gravatarHash != null) return false;
        if (name != null ? !name.equals(user.name) : user.name != null)
            return false;
        if (username != null ? !username.equals(user.username) :
            user.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result =
            31 * result + (gravatarHash != null ? gravatarHash.hashCode() : 0);
        return result;
    }
}
