package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.webcohesion.enunciate.metadata.Label;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * A list of results
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonPropertyOrder({ "results", "totalCount" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Label("Result List")
public class ResultList<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = -2149554068631922866L;
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
        if (o == null || getClass() != o.getClass()) return false;

        ResultList<?> that = (ResultList<?>) o;

        if (totalCount != that.totalCount) return false;
        return results != null ? results.equals(that.results) :
                that.results == null;
    }

    @Override
    public int hashCode() {
        int result = results != null ? results.hashCode() : 0;
        result = 31 * result + totalCount;
        return result;
    }
}
