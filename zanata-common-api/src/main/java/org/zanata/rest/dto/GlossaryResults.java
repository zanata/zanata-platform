package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.Namespaces;

/**
 * Wrapper for list of HGlossaryEntry/GlossaryEntry and list of warning message after
 * saving/update
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@XmlRootElement(name = "glossaryResults")
@JsonPropertyOrder({ "glossaryEntries", "warnings" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@XmlType(name = "glossaryResults", propOrder = {"glossaryEntries", "warnings"})
public class GlossaryResults implements Serializable {
    private List<GlossaryEntry> glossaryEntries;
    private List<String> warnings;

    public GlossaryResults() {
    }

    public GlossaryResults(List<GlossaryEntry> glossaryEntries, List<String> warnings) {
        this.glossaryEntries = glossaryEntries;
        this.warnings = warnings;
    }

    @JsonProperty("glossaryEntries")
    @XmlElementWrapper(name = "glossaryEntries", namespace = Namespaces.ZANATA_API)
    @XmlElementRef(namespace = Namespaces.ZANATA_API)
    public List<GlossaryEntry> getGlossaryEntries() {
        if (glossaryEntries == null) {
            glossaryEntries = new ArrayList<GlossaryEntry>();
        }
        return glossaryEntries;
    }

    @JsonProperty("warnings")
    @XmlElementWrapper(name = "warnings", namespace = Namespaces.ZANATA_API)
    public List<String> getWarnings() {
        if (warnings == null) {
            warnings = new ArrayList<String>();
        }
        return warnings;
    }

    public void setGlossaryEntries(List<GlossaryEntry> glossaryEntries) {
        this.glossaryEntries = glossaryEntries;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
