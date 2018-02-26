package org.zanata.rest.editor.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.ContentState;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    @Size(max = 500)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReviewData that = (ReviewData) o;

        if (transUnitId != null ? !transUnitId.equals(that.transUnitId) :
                that.transUnitId != null) return false;
        if (revision != null ? !revision.equals(that.revision) :
                that.revision != null) return false;
        if (comment != null ? !comment.equals(that.comment) :
                that.comment != null)
            return false;
        if (reviewCriteriaId != null ?
                !reviewCriteriaId.equals(that.reviewCriteriaId) :
                that.reviewCriteriaId != null) return false;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        int result = transUnitId != null ? transUnitId.hashCode() : 0;
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result +
                (reviewCriteriaId != null ? reviewCriteriaId.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }
}
