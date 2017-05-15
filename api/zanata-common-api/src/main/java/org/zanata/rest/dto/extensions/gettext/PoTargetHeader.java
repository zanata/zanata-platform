package org.zanata.rest.dto.extensions.gettext;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonTypeName;
import org.zanata.rest.dto.DTOUtil;

/**
 * Holds gettext file headers for a target document.
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@XmlType(name = "poTargetHeader", propOrder = { "comment", "entries" })
@XmlRootElement(name = "po-target-header")
@JsonTypeName(value = "po-target-header")
public class PoTargetHeader implements TranslationsResourceExtension {

    public static final String ID = "gettext";
    public static final String VERSION = PoHeader.VERSION;
    private static final long serialVersionUID = 7359524182201964786L;

    private String comment;
    private List<HeaderEntry> entries;

    public PoTargetHeader() {
    }

    public PoTargetHeader(String comment, HeaderEntry... entries) {
        setComment(comment);
        for (HeaderEntry entry : entries) {
            getEntries().add(entry);
        }
    }

    @XmlElement(name = "comment", required = true)
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @XmlElementWrapper(name = "entries", required = true)
    @XmlElement(name = "entry")
    public List<HeaderEntry> getEntries() {
        if (entries == null)
            entries = new ArrayList<HeaderEntry>();
        return entries;
    }

    @Override
    public String toString() {
        return DTOUtil.toXML(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + ((entries == null) ? 0 : entries.hashCode());
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
        if (!(obj instanceof PoTargetHeader)) {
            return false;
        }
        PoTargetHeader other = (PoTargetHeader) obj;
        if (comment == null) {
            if (other.comment != null) {
                return false;
            }
        } else if (!comment.equals(other.comment)) {
            return false;
        }
        if (entries == null) {
            if (other.entries != null) {
                return false;
            }
        } else if (!entries.equals(other.entries)) {
            return false;
        }
        return true;
    }

}
