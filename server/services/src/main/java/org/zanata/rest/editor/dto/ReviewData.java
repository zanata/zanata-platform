package org.zanata.rest.editor.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.ContentState;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Class for information on translation review requested from client and
 * response from server.
 *
 * @author Earl Floden <a href="mailto:efloden@redhat.com">efloden@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ReviewData implements Serializable {

    private static final long serialVersionUID = 1L;
    @NotNull
    private Long transUnitId;

    @NotNull
    private Integer revision;

    @JsonProperty("comment")
    private String comment;

    private Long reviewCriteriaId;

    @NotNull
    private ContentState status = ContentState.Rejected;

    public Long getTransUnitId() {
        return transUnitId;
    }

    public void setTransUnitId(Long transUnitId) {
        this.transUnitId = transUnitId;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getReviewCriteriaId() {
        return reviewCriteriaId;
    }

    public void setReviewCriteriaId(Long reviewCriteriaId) {
        this.reviewCriteriaId = reviewCriteriaId;
    }

    public ContentState getStatus() {
        return status;
    }

    public void setStatus(ContentState status) {
        this.status = status;
    }

}
