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

    @XmlAttribute(required = true)
    @NotNull
    public String getKey() {
        return key;
    }

    @XmlElement(nillable = true, namespace = Namespaces.ZANATA_API)
    public String getValue() {
        return value;
    }

    @XmlElement(name = "link", namespace = Namespaces.ZANATA_API)
    public Links getLinks() {
        return links;
    }

    @JsonIgnore
    public Links getLinks(boolean createIfNull) {
        if (createIfNull && links == null)
            links = new Links();
        return links;
    }

    @Override
    public String toString() {
        return DTOUtil.toXML(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Configuration))
            return false;
        final Configuration other = (Configuration) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$key = this.getKey();
        final Object other$key = other.getKey();
        if (this$key == null ? other$key != null : !this$key.equals(other$key))
            return false;
        final Object this$value = this.getValue();
        final Object other$value = other.getValue();
        if (this$value == null ? other$value != null
                : !this$value.equals(other$value))
            return false;
        final Object this$links = this.getLinks();
        final Object other$links = other.getLinks();
        if (this$links == null ? other$links != null
                : !this$links.equals(other$links))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Configuration;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $key = this.getKey();
        result = result * PRIME + ($key == null ? 43 : $key.hashCode());
        final Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        final Object $links = this.getLinks();
        result = result * PRIME + ($links == null ? 43 : $links.hashCode());
        return result;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setLinks(final Links links) {
        this.links = links;
    }

    public Configuration() {
    }
}
