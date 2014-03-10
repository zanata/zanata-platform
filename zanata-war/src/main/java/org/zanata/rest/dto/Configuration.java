package org.zanata.rest.dto;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.Namespaces;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@XmlType(name = "configurationType")
@XmlRootElement(name = "configuration")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Configuration implements Serializable {
    private static final long serialVersionUID = 1L;

    private String key;
    private String value;
    private Links links;

    public Configuration(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Configuration() {
    }

    @XmlAttribute(required = true)
    @NotNull
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @XmlElement(nillable = true, namespace = Namespaces.ZANATA_OLD)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @XmlElement(name = "link", namespace = Namespaces.ZANATA_API)
    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    @JsonIgnore
    public Links getLinks(boolean createIfNull) {
        if (createIfNull && links == null)
            links = new Links();
        return links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Configuration that = (Configuration) o;

        if (!key.equals(that.key)) return false;
        if (value != null ? !value.equals(that.value) : that.value != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return DTOUtil.toXML(this);
    }
}
