package org.zanata.rest.editor.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.rest.dto.resource.TextFlowTarget;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * TextFlowTarget with added fields needed by the editor.
 *
 */
@JsonPropertyOrder({ "resId", "state", "translator", "content", "contents",
        "sourceHash", "extensions", "lastChanged" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class EditorTextFlowTarget extends TextFlowTarget {

    private Date lastModifiedTime;

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

}
