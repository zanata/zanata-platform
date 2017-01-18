package org.zanata.webtrans.shared.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.common.base.MoreObjects;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

public class TransUnit implements IsSerializable, HasTransUnitId {
    private ContentState status;

    private TransUnitId id;
    private String resId;

    private LocaleId localeId;

    private boolean plural;
    private List<String> sources;
    private String sourceComment;
    private List<String> targets;
    private String msgContext;
    private String sourceRefs;
    private String sourceFlags;
    private String lastModifiedBy;
    private Date lastModifiedTime;
    private int rowIndex;
    private int verNum;
    private String targetComment;
    private int commentsCount;
    private String revisionComment;

    // for GWT
    @SuppressWarnings("unused")
    private TransUnit() {
    }

    private TransUnit(Builder builder) {
        this.id = builder.id;
        this.resId = builder.resId;
        this.localeId = builder.localeId;
        this.plural = builder.plural;
        this.sources = builder.sources;
        this.sourceComment = builder.sourceComment;
        this.targets = builder.targets;
        this.status = builder.status;
        this.lastModifiedBy = builder.lastModifiedBy;
        this.lastModifiedTime = builder.lastModifiedTime;
        this.msgContext = builder.msgContext;
        this.sourceRefs = builder.sourceRefs;
        this.sourceFlags = builder.sourceFlags;
        this.rowIndex = builder.rowIndex;
        this.verNum = builder.verNum;
        this.targetComment = builder.targetComment;
        this.commentsCount = builder.commentsCount;
        this.revisionComment = builder.revisionComment;
    }

    @Override
    public TransUnitId getId() {
        return id;
    }

    public String getResId() {
        return resId;
    }

    public LocaleId getLocaleId() {
        return localeId;
    }

    /**
     * @return the pluralSupported
     */
    public boolean isPlural() {
        return plural;
    }

    /**
     * @param plural
     *            the plural to set
     */
    void setPlural(boolean plural) {
        this.plural = plural;
    }

    public List<String> getSources() {
        return sources;
    }

    void setSources(List<String> sources) {
        this.sources = sources;
    }

    public String getSourceComment() {
        return sourceComment;
    }

    void setSourceComment(String sourceComment) {
        this.sourceComment = sourceComment;
    }

    public List<String> getTargets() {
        return targets;
    }

    void setTargets(List<String> targets) {
        this.targets = targets;
    }

    public ContentState getStatus() {
        return status;
    }

    void setStatus(ContentState status) {
        this.status = status;
    }

    public String getMsgContext() {
        return msgContext;
    }

    void setMsgContext(String msgContext) {
        this.msgContext = msgContext;
    }

    public String getSourceRefs() {
        return sourceRefs;
    }

    void setSourceRefs(String sourceRefs) {
        this.sourceRefs = sourceRefs;
    }

    public String getSourceFlags() {
        return sourceFlags;
    }

    void setSourceFlags(String sourceFlags) {
        this.sourceFlags = sourceFlags;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public Integer getVerNum() {
        return verNum;
    }

    void setVerNum(Integer verNum) {
        this.verNum = verNum;
    }

    public String getTargetComment() {
        return targetComment;
    }

    void setTargetComment(String targetComment) {
        this.targetComment = targetComment;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getRevisionComment() {
        return revisionComment;
    }

    public void setRevisionComment(String revisionComment) {
        this.revisionComment = revisionComment;
    }

    public String debugString() {
        // @formatter:off
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("rowIndex", rowIndex)
//                .add("resId", resId)
//                .add("verNum", verNum)
                .add("status", status)
//                .add("localeId", localeId)
//                .add("plural", plural)
//                .add("sources", sources)
//                .add("sourceComment", sourceComment)
//                .add("targets", targets)
//                .add("msgContext", msgContext)
//                .add("lastModifiedBy", lastModifiedBy)
//                .add("lastModifiedTime", lastModifiedTime)
                .toString();
        // @formatter:on
    }

    public static class Builder {
        private ContentState status = ContentState.New;
        private TransUnitId id;
        private String resId;
        private LocaleId localeId;
        private boolean plural;
        private List<String> sources = Lists.newArrayList();
        private String sourceComment;
        private List<String> targets = Lists.newArrayList();
        private String msgContext;
        private String sourceRefs;
        private String sourceFlags;
        private String lastModifiedBy;
        private Date lastModifiedTime;
        private int rowIndex;
        // to fail check if not set before build
        private int verNum = -1;
        private String targetComment;
        private int commentsCount;
        private String revisionComment;

        private Builder(TransUnit transUnit) {
            this.status = transUnit.status;
            this.id = transUnit.id;
            this.resId = transUnit.resId;
            this.localeId = transUnit.localeId;
            this.plural = transUnit.plural;
            this.sources = nullToEmpty(transUnit.sources);
            this.sourceComment = transUnit.sourceComment;
            this.targets = nullToEmpty(transUnit.targets);
            this.msgContext = transUnit.msgContext;
            this.sourceRefs = transUnit.sourceRefs;
            this.sourceFlags = transUnit.sourceFlags;
            this.lastModifiedBy = transUnit.lastModifiedBy;
            this.lastModifiedTime = transUnit.lastModifiedTime;
            this.rowIndex = transUnit.rowIndex;
            this.verNum = transUnit.verNum;
            this.commentsCount = transUnit.commentsCount;
            this.revisionComment = transUnit.revisionComment;
        }

        private Builder() {
        }

        public TransUnit build() {
            Preconditions.checkNotNull(id, "transUnitId can not be null");
            Preconditions.checkNotNull(resId, "resId can not be null");
            Preconditions.checkNotNull(localeId, "localeId can not be null");
            Preconditions.checkState(sources != null && !sources.isEmpty());
            Preconditions.checkState(rowIndex >= 0);
            Preconditions.checkState(verNum >= 0);

            lastModifiedBy = Strings.nullToEmpty(lastModifiedBy);
            status = MoreObjects.firstNonNull(status, ContentState.New);

            return new TransUnit(this);
        }

        public static Builder newTransUnitBuilder() {
            return new Builder();
        }

        public static Builder from(TransUnit transUnit) {
            return new Builder(transUnit);
        }

        private static <T> List<T> nullToEmpty(List<T> contents) {
            return contents == null ? Lists.<T> newArrayList() : contents;
        }

        public Builder setStatus(ContentState status) {
            this.status = status;
            return this;
        }

        public Builder setId(TransUnitId id) {
            this.id = id;
            return this;
        }

        public Builder setId(long id) {
            this.id = new TransUnitId(id);
            return this;
        }

        public Builder setResId(String resId) {
            this.resId = resId;
            return this;
        }

        public Builder setLocaleId(LocaleId localeId) {
            this.localeId = localeId;
            return this;
        }

        public Builder setLocaleId(String localeString) {
            this.localeId = new LocaleId(localeString);
            return this;
        }

        public Builder setPlural(boolean plural) {
            this.plural = plural;
            return this;
        }

        public Builder setSources(List<String> sources) {
            this.sources = nullToEmpty(sources);
            return this;
        }

        public Builder addSource(String... sourceStrings) {
            Collections.addAll(sources, sourceStrings);
            return this;
        }

        public Builder setSourceComment(String sourceComment) {
            this.sourceComment = sourceComment;
            return this;
        }

        public Builder setTargets(List<String> targets) {
            this.targets = nullToEmpty(targets);
            return this;
        }

        public Builder addTargets(String... targetStrings) {
            Collections.addAll(targets, targetStrings);
            return this;
        }

        public Builder setMsgContext(String msgContext) {
            this.msgContext = msgContext;
            return this;
        }

        public Builder setSourceRefs(String sourceRefs) {
            this.sourceRefs = sourceRefs;
            return this;
        }

        public Builder setSourceFlags(String sourceFlags) {
            this.sourceFlags = sourceFlags;
            return this;
        }

        public Builder setLastModifiedBy(String lastModifiedBy) {
            this.lastModifiedBy = lastModifiedBy;
            return this;
        }

        public Builder setLastModifiedTime(Date lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
            return this;
        }

        public Builder setRowIndex(int rowIndex) {
            this.rowIndex = rowIndex;
            return this;
        }

        public Builder setVerNum(int verNum) {
            this.verNum = verNum;
            return this;
        }

        public Builder setTargetComment(String targetComment) {
            this.targetComment = targetComment;
            return this;
        }

        public Builder setCommentsCount(int commentsCount) {
            this.commentsCount = commentsCount;
            return this;
        }

        public Builder setRevisionComment(String revisionComment) {
            this.revisionComment = revisionComment;
            return this;
        }
    }
}
