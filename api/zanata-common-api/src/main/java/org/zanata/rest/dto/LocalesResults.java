package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LocalesResults implements Serializable {
    private static final long serialVersionUID = 6238439741333311645L;
    public Integer totalCount;
    public List<LanguageTeamSearchResult> results;

    @java.beans.ConstructorProperties({ "totalCount", "results" })
    public LocalesResults(final int totalCount,
            final List<LanguageTeamSearchResult> results) {
        this.totalCount = totalCount;
        this.results = results;
    }

    @JsonProperty("totalCount")
    public Integer getTotalCount() {
        return this.totalCount;
    }

    @JsonProperty("results")
    public List<LanguageTeamSearchResult> getResults() {
        return this.results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalesResults that = (LocalesResults) o;

        if (totalCount != null ? !totalCount.equals(that.totalCount) :
                that.totalCount != null) return false;
        return results != null ? results.equals(that.results) :
                that.results == null;
    }

    @Override
    public int hashCode() {
        int result = totalCount != null ? totalCount.hashCode() : 0;
        result = 31 * result + (results != null ? results.hashCode() : 0);
        return result;
    }
}
