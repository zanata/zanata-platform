package org.zanata.rest.dto.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.ContentState;
import org.zanata.common.Namespaces;
import org.zanata.rest.dto.DTOUtil;
import org.zanata.rest.dto.Extensible;
import org.zanata.rest.dto.Person;
import org.zanata.rest.dto.extensions.gettext.TextFlowTargetExtension;

@XmlType(name = "textFlowTargetType", propOrder = { "description",
        "translator", "content", "contents", "extensions" })
@JsonPropertyOrder({ "resId", "state", "translator", "content", "contents",
        "extensions" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TextFlowTarget extends TextContainer implements Serializable,
        Extensible<TextFlowTargetExtension> {

    private static final long serialVersionUID = 1L;
    private String resId;
    private ContentState state = ContentState.New;
    private Person translator;
    private String description;
    private ExtensionSet<TextFlowTargetExtension> extensions;
    private Integer revision;
    private Integer textFlowRevision;

    public TextFlowTarget() {
    }

    public TextFlowTarget(String resId) {
        this.resId = resId;
    }

    @XmlElement(name = "person", namespace = Namespaces.ZANATA_API)
    public Person getTranslator() {
        return translator;
    }

    public void setTranslator(Person translator) {
        this.translator = translator;
    }

    @XmlAttribute(name = "state", required = true)
    public ContentState getState() {
        return state;
    }

    public void setState(ContentState state) {
        this.state = state;
    }

    /**
     * Optional descriptive text to identify the TextFlowTarget, eg an
     * abbreviated version of the source text being translated. This can be used
     * for a more readable XML serialisation.
     *
     * @return
     */
    @XmlElement(name = "description", required = false,
            namespace = Namespaces.ZANATA_OLD)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElementWrapper(name = "extensions", required = false,
            namespace = Namespaces.ZANATA_OLD)
    @XmlElement(name = "extension", namespace = Namespaces.ZANATA_OLD)
    public ExtensionSet<TextFlowTargetExtension> getExtensions() {
        return extensions;
    }

    @JsonIgnore
    public ExtensionSet<TextFlowTargetExtension> getExtensions(
            boolean createIfNull) {
        if (createIfNull && extensions == null)
            extensions = new ExtensionSet<TextFlowTargetExtension>();
        return extensions;
    }

    public void setExtensions(ExtensionSet<TextFlowTargetExtension> extensions) {
        this.extensions = extensions;
    }

    @XmlAttribute(name = "res-id", required = true)
    @NotEmpty
    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    @Override
    public String toString() {
        return DTOUtil.toXML(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result =
                prime * result
                        + ((description == null) ? 0 : description.hashCode());
        result =
                prime * result
                        + ((extensions == null) ? 0 : extensions.hashCode());
        result = prime * result + ((resId == null) ? 0 : resId.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result =
                prime * result
                        + ((translator == null) ? 0 : translator.hashCode());
        result =
                prime * result + ((revision == null) ? 0 : revision.hashCode());
        result =
                prime
                        * result
                        + ((textFlowRevision == null) ? 0 : textFlowRevision
                                .hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof TextFlowTarget)) {
            return false;
        }
        TextFlowTarget other = (TextFlowTarget) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (extensions == null) {
            if (other.extensions != null) {
                return false;
            }
        } else if (!extensions.equals(other.extensions)) {
            return false;
        }
        if (resId == null) {
            if (other.resId != null) {
                return false;
            }
        } else if (!resId.equals(other.resId)) {
            return false;
        }
        if (state != other.state) {
            return false;
        }
        if (translator == null) {
            if (other.translator != null) {
                return false;
            }
        } else if (!translator.equals(other.translator)) {
            return false;
        }
        if (revision == null) {
            if (other.revision != null) {
                return false;
            }
        } else if (!revision.equals(other.revision)) {
            return false;
        }
        if (textFlowRevision == null) {
            if (other.textFlowRevision != null) {
                return false;
            }
        } else if (!textFlowRevision.equals(other.textFlowRevision)) {
            return false;
        }
        return true;
    }

    @XmlAttribute(name = "revision", required = false)
    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer i) {
        revision = i;
    }

    @XmlAttribute(name = "resourceRevision", required = false)
    public Integer getTextFlowRevision() {
        return textFlowRevision;
    }

    public void setTextFlowRevision(Integer textFlowRevision) {
        this.textFlowRevision = textFlowRevision;
    }

}
