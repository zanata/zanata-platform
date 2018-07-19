package org.zanata.rest.editor.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.rest.dto.resource.TextFlowTarget;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

/**
 * TextFlowTarget with added fields needed by the editor.
 *
 */
@JsonPropertyOrder({ "resId", "state", "translator", "content", "contents",
        "sourceHash", "extensions", "lastChanged", "transSourceType" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class EditorTextFlowTarget extends TextFlowTarget {

    private static final long serialVersionUID = 6861528105976043809L;
    private Date lastModifiedTime;
    private String transSourceType;

    public EditorTextFlowTarget() {
        super();
    }

    public EditorTextFlowTarget(String resId) {
        super(resId);
    }

    public void setLastModifiedTime(@NotNull Date lastModifiedTime) {
        this.lastModifiedTime = new Date(lastModifiedTime.getTime());
    }

    public Date getLastModifiedTime() {
        return new Date(this.lastModifiedTime.getTime());
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
        if (!super.equals(o)) return false;
        EditorTextFlowTarget that = (EditorTextFlowTarget) o;
        return Objects.equals(lastModifiedTime, that.lastModifiedTime) &&
            Objects.equals(transSourceType, that.transSourceType);
    }

    @Override
    public int hashCode() {

        return Objects
            .hash(super.hashCode(), lastModifiedTime, transSourceType);
    }
}
