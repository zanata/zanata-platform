package org.zanata.rest.dto.resource;

import com.webcohesion.enunciate.metadata.Label;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.Namespaces;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * A series of text flows to be translated and sharing common metadata.
 */
@XmlType(name = "resourceType", propOrder = { "textFlows" })
@XmlRootElement(name = "resource")
@JsonPropertyOrder({ "name", "contentType", "lang", "extensions", "textFlows" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Label("Resource")
public class Resource extends AbstractResourceMeta {

    private static final long serialVersionUID = 1L;
    private List<TextFlow> textFlows;

    public Resource() {
    }

    public Resource(String name) {
        super(name);
    }

    /**
     * Set of text flows containing the translatable strings.
     */
    @XmlElementWrapper(name = "text-flows", required = false,
            namespace = Namespaces.ZANATA_OLD)
    @XmlElement(name = "text-flow",
            namespace = org.zanata.common.Namespaces.ZANATA_API)
    @JsonProperty("textFlows")
    public List<TextFlow> getTextFlows() {
        if (textFlows == null) {
            textFlows = new ArrayList<TextFlow>();
        }
        return textFlows;
    }

    // @Override
    // public String toString()
    // {
    // return DTOUtil.toXML(this);
    // }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCodeHelper();
        result =
                prime * result
                        + ((textFlows == null) ? 0 : textFlows.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Resource)) {
            return false;
        }
        Resource other = (Resource) obj;
        if (!super.equalsHelper(other)) {
            return false;
        }
        if (textFlows == null) {
            if (other.textFlows != null) {
                return false;
            }
        } else if (!textFlows.equals(other.textFlows)) {
            return false;
        }
        return true;
    }

}
