package org.zanata.rest.editor.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.rest.dto.resource.TextFlowTarget;

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

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public Date getLastModifiedTime() {
        return this.lastModifiedTime;
    }

}
