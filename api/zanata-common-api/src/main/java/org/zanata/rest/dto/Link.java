package org.zanata.rest.dto;

import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.metadata.Label;

import java.net.URI;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * A single link to reference a URI
 */
@XmlType(name = "linkType")
@Label("Link")
public class Link {

    private URI href;
    private String rel;
    private String type;

    protected Link() {
    }

    public Link(URI href) {
        this.href = href;
    }

    public Link(URI href, String rel) {
        this.href = href;
        this.rel = rel;
    }

    public Link(URI href, String rel, String type) {
        this.href = href;
        this.rel = rel;
        this.type = type;
    }

    /**
     * The URI reference by this link
     */
    @XmlAttribute(name = "href", required = true)
    @DocumentationExample(value = "http://alink.com")
    public URI getHref() {
        return href;
    }

    public void setHref(URI href) {
        this.href = href;
    }

    /**
     * The relationship this link holds to its parent object
     */
    @XmlAttribute(name = "rel", required = false)
    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    /**
     * The type of link
     */
    @XmlAttribute(name = "type", required = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Link{");
        sb.append("href=").append(href);
        sb.append(", rel='").append(rel).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((href == null) ? 0 : href.hashCode());
        result = prime * result + ((rel == null) ? 0 : rel.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        if (!(obj instanceof Link)) {
            return false;
        }
        Link other = (Link) obj;
        if (href == null) {
            if (other.href != null) {
                return false;
            }
        } else if (!href.equals(other.href)) {
            return false;
        }
        if (rel == null) {
            if (other.rel != null) {
                return false;
            }
        } else if (!rel.equals(other.rel)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

}
