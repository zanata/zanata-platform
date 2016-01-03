package org.zanata.rest.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.ContentState;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "id", "resId", "status"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TransUnitStatus implements Serializable {

    private Long id;
    private String resId;
    private ContentState status;

    public TransUnitStatus() {
        this(null, null, null);
    }

    public TransUnitStatus(Long id, String resId, ContentState status) {
        this.id = id;
        this.resId = resId;
        this.status = status;
    }

    @NotNull
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotNull
    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    @NotNull
    public ContentState getStatus() {
        return status;
    }

    public void setStatus(ContentState status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransUnitStatus)) return false;

        TransUnitStatus status1 = (TransUnitStatus) o;

        if (id != null ? !id.equals(status1.id) : status1.id != null)
            return false;
        if (resId != null ? !resId.equals(status1.resId) :
            status1.resId != null)
            return false;
        if (status != status1.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (resId != null ? resId.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }
}
