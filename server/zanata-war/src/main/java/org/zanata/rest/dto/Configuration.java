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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@XmlType(name = "configurationType")
@XmlRootElement(name = "configuration")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@EqualsAndHashCode
@Setter
@NoArgsConstructor
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
}
