package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.TransUnitId;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class AddReviewComment implements SessionEventData {
    private static final long serialVersionUID = 1L;
    private TransUnitId transUnitId;
    private int commentCount;

    @SuppressWarnings("unused")
    public AddReviewComment() {
    }

    public AddReviewComment(TransUnitId transUnitId, int commentCount) {
        this.transUnitId = transUnitId;
        this.commentCount = commentCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public TransUnitId getTransUnitId() {
        return transUnitId;
    }
}
