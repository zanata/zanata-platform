package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.ContentState;

/***
 * INTERNAL API ONLY - SUBJECT TO CHANGE OR REMOVAL WITHOUT NOTICE <br/>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "id", "resId", "status", "transSourceType"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TransUnitStatus implements Serializable {

    private static final long serialVersionUID = -6155963443005474428L;
    private Long id;
    private String resId;
    private ContentState status;
    private String transSourceType;

    public TransUnitStatus() {
        this(null, null, null, null);
    }

    public TransUnitStatus(Long id, String resId, ContentState status) {
        this(id, resId, status, null);
    }

    public TransUnitStatus(Long id, String resId, ContentState status,
        String transSourceType) {
        this.id = id;
        this.resId = resId;
        this.status = status;
        this.transSourceType = transSourceType;
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

    public String getTransSourceType() {
        return transSourceType;
    }

    public void setTransSourceType(String transSourceType) {
        this.transSourceType = transSourceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransUnitStatus that = (TransUnitStatus) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(resId, that.resId) &&
            status == that.status &&
            Objects.equals(transSourceType, that.transSourceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, resId, status, transSourceType);
    }
}
