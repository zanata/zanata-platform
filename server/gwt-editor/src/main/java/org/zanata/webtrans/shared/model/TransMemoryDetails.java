package org.zanata.webtrans.shared.model;

import java.util.Date;

import com.google.common.base.MoreObjects;
import org.zanata.common.ContentState;

import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

public class TransMemoryDetails implements IsSerializable {

    private String sourceComment;
    private String targetComment;
    private String projectName;
    private String iterationName;
    private String docId;
    private String resId;
    private String msgContext;
    private ContentState state;
    private String lastModifiedBy;
    private Date lastModifiedDate;
    private String url;

    @SuppressWarnings("unused")
    private TransMemoryDetails() {
    }

    public TransMemoryDetails(String sourceComment, String targetComment,
            String projectName, String iterationName, String docId,
            String resId, String msgContext, ContentState state,
            String lastModifiedBy, Date lastModifiedDate, String url) {
        this.sourceComment = sourceComment;
        this.targetComment = targetComment;
        this.projectName = projectName;
        this.iterationName = iterationName;
        this.docId = docId;
        this.resId = resId;
        this.msgContext = msgContext;
        this.state = state;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
        this.url = url;
    }

    public String getSourceComment() {
        return sourceComment;
    }

    public String getTargetComment() {
        return targetComment;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getIterationName() {
        return iterationName;
    }

    public String getDocId() {
        return docId;
    }

    public String getResId() {
        return resId;
    }

    public String getMsgContext() {
        return msgContext;
    }

    public ContentState getState() {
        return state;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        // @formatter:off
      return MoreObjects.toStringHelper(this).
            add("sourceComment", sourceComment).
            add("targetComment", targetComment).
            add("projectName", projectName).
            add("iterationName", iterationName).
            add("docId", docId).
            add("resId", resId).
            add("msgContext", msgContext).
            add("state", state).
            add("lastModifiedBy", lastModifiedBy).
            add("lastModifiedDate", lastModifiedDate).
            add("url", url).
            toString();
      // @formatter:on
    }
}
