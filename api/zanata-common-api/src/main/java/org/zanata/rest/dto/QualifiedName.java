package org.zanata.rest.dto;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.Namespaces;
import org.zanata.rest.MediaTypes;

/**
 * Object Glossary qualified name. Usage:
 * {@link GlossaryEntry#getQualifiedName()}
 * {@link org.zanata.rest.service.GlossaryResource}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@XmlRootElement(name = "qualifiedName")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class QualifiedName implements Serializable, HasMediaType {
    private String name;

    public QualifiedName() {
    }

    public QualifiedName(String name) {
        this.name = name;
    }

    @XmlElement(name = "name", required = false,
        namespace = Namespaces.ZANATA_API)
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QualifiedName)) return false;

        QualifiedName that = (QualifiedName) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String getMediaType(MediaTypes.Format format) {
        return MediaTypes.APPLICATION_ZANATA_GLOSSARY + format;
    }
}
