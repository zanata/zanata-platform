package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonPropertyOrder({ "results", "totalCount" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResultList<T extends Serializable> implements Serializable{

    private List<T> results;

    private int totalCount;

    public List<T> getResults() {
        if (results == null) {
            results = new ArrayList<T>();
        }
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResultList)) return false;

        ResultList that = (ResultList) o;

        if (totalCount != that.totalCount) return false;
        if (results != null ? !results.equals(that.results) :
            that.results != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = results != null ? results.hashCode() : 0;
        result = 31 * result + totalCount;
        return result;
    }
}
