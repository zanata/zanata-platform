package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.Date;

import com.google.common.base.Strings;
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
    private String commenterName;
    private String accountName;
    private Date creationDate;

    public ReviewComment() {
    }

    public ReviewComment(ReviewCommentId id, String comment,
            String commenterName, Date creationDate) {
        this.id = id;
        this.comment = comment;
        this.commenterName = commenterName;
        this.creationDate = creationDate != null ?
                new Date(creationDate.getTime()) : null;
    }

    public ReviewCommentId getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public Date getCreationDate() {
        return creationDate != null ?
                new Date(creationDate.getTime()) : null;
    }

    public ReviewComment setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    public String getAccountName() {
        return Strings.nullToEmpty(accountName);
    }

    @Override
    protected Date getDate() {
        return creationDate;
    }
}
