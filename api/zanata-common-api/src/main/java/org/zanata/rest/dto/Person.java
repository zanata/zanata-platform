package org.zanata.rest.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.Namespaces;

@XmlType(name = "personType")
@XmlRootElement(name = "person")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "email", "name", "links" })
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Person implements Serializable, HasSample<Person> {

    private static final long serialVersionUID = 7686060899220005351L;
    private String email;
    private String name;

    private Links links;

    public Person() {
    }

    public Person(String email, String name) {
        this.email = email;
        this.name = name;
    }

    @XmlAttribute(name = "email", required = true)
    @Email
    @NotNull
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @XmlAttribute(name = "name", required = true)
    @NotEmpty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set of links managed by this resource
     *
     * This field is ignored in PUT/POST operations
     *
     * @return set of Links managed by this resource
     */
    @XmlElement(name = "link", required = false,
            namespace = Namespaces.ZANATA_OLD)
    @JsonProperty("links")
    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public Links getLinks(boolean createIfNull) {
        if (createIfNull && links == null)
            links = new Links();
        return links;
    }

    @Override
    public Person createSample() {
        return new Person("me@example.com", "Mr. Example");
    }

    @Override
    public String toString() {
        return DTOUtil.toXML(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((links == null) ? 0 : links.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Person)) {
            return false;
        }
        Person other = (Person) obj;
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        } else if (!email.equals(other.email)) {
            return false;
        }
        if (links == null) {
            if (other.links != null) {
                return false;
            }
        } else if (!links.equals(other.links)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
