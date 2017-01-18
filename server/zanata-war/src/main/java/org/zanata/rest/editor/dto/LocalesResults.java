package org.zanata.rest.editor.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.rest.search.dto.LanguageTeamSearchResult;

import java.io.Serializable;
import java.util.List;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LocalesResults implements Serializable {
    public int totalCount;

    public List<LanguageTeamSearchResult> results;

    @java.beans.ConstructorProperties({ "totalCount", "results" })
    public LocalesResults(int totalCount,
            List<LanguageTeamSearchResult> results) {
        this.totalCount = totalCount;
        this.results = results;
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public List<LanguageTeamSearchResult> getResults() {
        return this.results;
    }
}
