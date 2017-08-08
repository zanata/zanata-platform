package org.zanata.rest.dto.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.webcohesion.enunciate.metadata.Label;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.common.Namespaces;
import org.zanata.common.ResourceType;
import org.zanata.rest.dto.ContentTypeAdapter;
import org.zanata.rest.dto.Extensible;
import org.zanata.rest.dto.LocaleIdAdapter;
import org.zanata.rest.dto.extensions.gettext.AbstractResourceMetaExtension;

@XmlType(name = "abstractResourceMetaType",
        propOrder = { "name", "extensions" })
@Label("Abstract Resource")
public abstract class AbstractResourceMeta implements Serializable,
        Extensible<AbstractResourceMetaExtension> {
    private static final long serialVersionUID = 1L;

    private String name;

    private ContentType contentType = ContentType.TextPlain;

    private ResourceType type = ResourceType.FILE;

    private LocaleId lang = LocaleId.EN_US;

    private ExtensionSet<AbstractResourceMetaExtension> extensions;

    private Integer revision;

    // TODO add Links for Resource, ResourceMeta and TranslationsResource

    public AbstractResourceMeta() {
    }

    public AbstractResourceMeta(String name) {
        this.name = name;
    }

    @XmlElementWrapper(name = "extensions", required = false,
            namespace = Namespaces.ZANATA_OLD)
    @XmlElement(name = "extension", namespace = Namespaces.ZANATA_OLD)
    public ExtensionSet<AbstractResourceMetaExtension> getExtensions() {
        return extensions;
    }

    public void setExtensions(
            ExtensionSet<AbstractResourceMetaExtension> extensions) {
        this.extensions = extensions;
    }

    @JsonIgnore
    public ExtensionSet<AbstractResourceMetaExtension> getExtensions(
            boolean createIfNull) {
        if (createIfNull && extensions == null)
            extensions = new ExtensionSet<AbstractResourceMetaExtension>();
        return extensions;
    }

    @XmlAttribute(name = "type", required = true)
    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
    @XmlAttribute(name = "lang", namespace = Namespaces.XML, required = true)
    public LocaleId getLang() {
        return lang;
    }

    public void setLang(LocaleId lang) {
        this.lang = lang;
    }

    @XmlJavaTypeAdapter(type = ContentType.class,
            value = ContentTypeAdapter.class)
    @XmlAttribute(name = "content-type", required = true)
    @JsonProperty("contentType")
    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    @XmlElement(name = "name", required = true,
            namespace = Namespaces.ZANATA_OLD)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute()
    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    // @Override
    // public String toString()
    // {
    // return DTOUtil.toXML(this);
    // }

    /**
     * Helper method for equals in subclasses.This abstract class does not
     * implement equals or hashCode, because a Resource should not be equal to a
     * ResourceMeta.
     */
    protected int hashCodeHelper() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((contentType == null) ? 0 : contentType.hashCode());
        result =
                prime * result
                        + ((extensions == null) ? 0 : extensions.hashCode());
        result = prime * result + ((lang == null) ? 0 : lang.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result =
                prime * result + ((revision == null) ? 0 : revision.hashCode());
        return result;
    }

    /**
     * Helper method for equals in subclasses.This abstract class does not
     * implement equals or hashCode, because a Resource should not be equal to a
     * ResourceMeta.
     */
    protected boolean equalsHelper(AbstractResourceMeta other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (contentType == null) {
            if (other.contentType != null) {
                return false;
            }
        } else if (!contentType.equals(other.contentType)) {
            return false;
        }
        if (extensions == null) {
            if (other.extensions != null) {
                return false;
            }
        } else if (!extensions.equals(other.extensions)) {
            return false;
        }
        if (lang == null) {
            if (other.lang != null) {
                return false;
            }
        } else if (!lang.equals(other.lang)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (revision == null) {
            if (other.revision != null) {
                return false;
            }
        } else if (!revision.equals(other.revision)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

}
