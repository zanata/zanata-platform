package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Wrapper for list of HGlossaryEntry/GlossaryEntry and list of warning message after
 * saving/update
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonPropertyOrder({ "glossaryEntries", "warnings" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class GlossaryResults<T> implements Serializable{
    private List<T> glossaryEntries;
    private List<String> warnings;

    public GlossaryResults() {
    }

    public GlossaryResults(List<T> glossaryEntries, List<String> warnings) {
        this.glossaryEntries = glossaryEntries;
        this.warnings = warnings;
    }

    @JsonProperty("glossaryEntries")
    public List<T> getGlossaryEntries() {
        if (glossaryEntries == null) {
            glossaryEntries = new ArrayList<T>();
        }
        return glossaryEntries;
    }

    @JsonProperty("warnings")
    public List<String> getWarnings() {
        if (warnings == null) {
            warnings = new ArrayList<String>();
        }
        return warnings;
    }

    public void setGlossaryEntries(List<T> glossaryEntries) {
        this.glossaryEntries = glossaryEntries;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
