package org.zanata.webtrans.shared.rpc;

import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class EditorFilter implements IsSerializable {
    public static final EditorFilter ALL = new EditorFilter();
    private String textInContent;
    private String resId;
    private String lastModifiedBefore;
    private String lastModifiedAfter;
    private String lastModifiedByUser;
    private String sourceComment;
    private String transComment;
    private String msgContext;

    private EditorFilter() {
    }

    private EditorFilter(String textInContent, String resId,
            String lastModifiedBefore, String lastModifiedAfter,
            String lastModifiedByUser, String sourceComment,
            String transComment, String msgContext) {
        this.textInContent = textInContent;
        this.resId = resId;
        this.lastModifiedBefore = lastModifiedBefore;
        this.lastModifiedAfter = lastModifiedAfter;
        this.lastModifiedByUser = lastModifiedByUser;
        this.sourceComment = sourceComment;
        this.transComment = transComment;
        this.msgContext = msgContext;
    }

    public EditorFilter(EditorFilter o) {
        this.textInContent = o.textInContent;
        this.resId = o.resId;
        this.lastModifiedBefore = o.lastModifiedBefore;
        this.lastModifiedAfter = o.lastModifiedAfter;
        this.lastModifiedByUser = o.lastModifiedByUser;
        this.sourceComment = o.sourceComment;
        this.transComment = o.transComment;
        this.msgContext = o.msgContext;
    }

    public String getTextInContent() {
        return textInContent;
    }

    public String getResId() {
        return resId;
    }

    public String getLastModifiedBefore() {
        return lastModifiedBefore;
    }

    public String getLastModifiedAfter() {
        return lastModifiedAfter;
    }

    public String getLastModifiedByUser() {
        return lastModifiedByUser;
    }

    public String getSourceComment() {
        return sourceComment;
    }

    public String getTransComment() {
        return transComment;
    }

    public String getMsgContext() {
        return msgContext;
    }

    public EditorFilter changeTextInContent(String findMessage) {
        EditorFilter result = new EditorFilter(this);
        result.textInContent = findMessage;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        EditorFilter that = (EditorFilter) o;

        return Objects.equal(lastModifiedAfter, that.lastModifiedAfter)
                && Objects.equal(lastModifiedBefore, that.lastModifiedBefore)
                && Objects.equal(lastModifiedByUser, that.lastModifiedByUser)
                && Objects.equal(msgContext, that.msgContext)
                && Objects.equal(resId, that.resId)
                && Objects.equal(sourceComment, that.sourceComment)
                && Objects.equal(textInContent, that.textInContent)
                && Objects.equal(transComment, that.transComment);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(textInContent, resId, lastModifiedAfter,
                lastModifiedBefore, lastModifiedByUser, sourceComment,
                transComment, msgContext);
    }

    public boolean isAcceptAll() {
        return this.equals(ALL);
    }
}
