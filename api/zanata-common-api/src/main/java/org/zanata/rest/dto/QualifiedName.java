package org.zanata.rest.dto;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.metadata.Label;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.Namespaces;
import org.zanata.rest.MediaTypes;

/**
 * Describes a qualified system name. Usage:
 * {@link GlossaryEntry#getQualifiedName()}
 * {@link org.zanata.rest.service.GlossaryResource}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@XmlRootElement(name = "qualifiedName")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Label("Qualified Name")
public class QualifiedName implements Serializable, HasMediaType {
    private static final long serialVersionUID = 934216980812012602L;
    private String name;

    public QualifiedName() {
    }

    public QualifiedName(String name) {
        this.name = name;
    }

    @XmlElement(name = "name", required = false,
        namespace = Namespaces.ZANATA_API)
    @JsonProperty("name")
    @DocumentationExample("global/default")
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
