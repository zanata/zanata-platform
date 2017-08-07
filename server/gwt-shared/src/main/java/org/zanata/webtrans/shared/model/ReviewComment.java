package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.zanata.common.ContentState;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ReviewComment extends ComparableByDate implements IsSerializable,
        Serializable {
    private static final long serialVersionUID = -739227847010028336L;
    private ReviewCommentId id;
    private String comment;
    private List<String> targetContents;
    private String commenterName;
    private Date creationDate;
    private ContentState targetState;
    private Integer targetVersion;

    public ReviewComment() {
    }

    public ReviewComment(ReviewCommentId id, String comment,
            String commenterName, Date creationDate, Integer targetVersion) {
        this.id = id;
        this.comment = comment;
        this.commenterName = commenterName;
        this.creationDate = creationDate != null ?
                new Date(creationDate.getTime()) : null;
        this.targetVersion = targetVersion;
    }

    public ReviewComment attachMetaInfo(List<String> targetContents,
            ContentState targetState) {
        this.targetContents = targetContents;
        this.targetState = targetState;
        return this;
    }

    public ReviewCommentId getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    public List<String> getTargetContents() {
        return targetContents;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public Date getCreationDate() {
        return creationDate != null ?
                new Date(creationDate.getTime()) : null;
    }

    public ContentState getTargetState() {
        return targetState;
    }

    public Integer getTargetVersion() {
        return targetVersion;
    }

    @Override
    protected Date getDate() {
        return creationDate;
    }
}
