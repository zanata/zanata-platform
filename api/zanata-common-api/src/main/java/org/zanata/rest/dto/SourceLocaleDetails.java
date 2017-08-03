package org.zanata.rest.dto;

import javax.annotation.Nullable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"docCount", "localeDetails"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SourceLocaleDetails implements Serializable {
    private static final long serialVersionUID = -155454847734832690L;
    private Integer docCount;
    // This will be null for total doc count
    private @Nullable LocaleDetails localeDetails;

    public SourceLocaleDetails() {
        this(null, null);
    }

    public SourceLocaleDetails(Integer docCount, LocaleDetails localeDetails) {
        this.docCount = docCount;
        this.localeDetails = localeDetails;
    }

    @JsonProperty("docCount")
    public Integer getDocCount() {
        return docCount;
    }

    public void setDocCount(Integer docCount) {
        this.docCount = docCount;
    }

    @JsonProperty("localeDetails")
    public LocaleDetails getLocaleDetails() {
        return localeDetails;
    }

    public void setLocaleDetails(LocaleDetails localeDetails) {
        this.localeDetails = localeDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceLocaleDetails details = (SourceLocaleDetails) o;

        if (docCount != null ? !docCount.equals(details.docCount) :
                details.docCount != null) return false;
        return localeDetails != null ?
                localeDetails.equals(details.localeDetails) :
                details.localeDetails == null;
    }

    @Override
    public int hashCode() {
        int result = docCount != null ? docCount.hashCode() : 0;
        result =
                31 * result +
                        (localeDetails != null ? localeDetails.hashCode() : 0);
        return result;
    }
}
