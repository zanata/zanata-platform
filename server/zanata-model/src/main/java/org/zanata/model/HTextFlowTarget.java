/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.AnalyzerDiscriminator;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Parameter;
import org.zanata.common.ContentState;
import org.zanata.common.HasContents;
import org.zanata.common.LocaleId;
import org.zanata.hibernate.search.ContentStateBridge;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.hibernate.search.LocaleIdBridge;
import org.zanata.hibernate.search.StringListBridge;
import org.zanata.hibernate.search.TextContainerAnalyzerDiscriminator;
import org.zanata.model.type.EntityType;
import org.zanata.model.type.EntityTypeType;
import org.zanata.model.type.TranslationSourceType;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.zanata.model.type.TranslationSourceTypeType;

/**
 * Represents a flow of translated text that should be processed as a
 * stand-alone structural unit.
 *
 * @see org.zanata.rest.dto.resource.TextFlowTarget
 * @author Asgeir Frimannsson <asgeirf@redhat.com>
 */
@Entity
@EntityListeners({ HTextFlowTarget.EntityListener.class })
@Cacheable
@TypeDefs({
        @TypeDef(name = "sourceType",
                typeClass = TranslationSourceTypeType.class),
        @TypeDef(name = "entityType", typeClass = EntityTypeType.class) })
@Indexed
public class HTextFlowTarget extends ModelEntityBase
        implements HasContents, HasSimpleComment, ITextFlowTargetHistory,
        Serializable, ITextFlowTarget, IsEntityWithType {
    private static final long serialVersionUID = 302308010797605435L;
    private HTextFlow textFlow;
    @Nonnull
    private HLocale locale;
    private String content0;
    private String content1;
    private String content2;
    private String content3;
    private String content4;
    private String content5;
    private ContentState state = ContentState.New;
    private Integer textFlowRevision;
    private HPerson lastModifiedBy;
    private HPerson translator;
    private HPerson reviewer;
    private HSimpleComment comment;
    private Map<Integer, HTextFlowTargetHistory> history;
    private List<HTextFlowTargetReviewComment> reviewComments;
    private String revisionComment;
    private EntityType copiedEntityType;
    private Long copiedEntityId;
    private TranslationSourceType sourceType;
    private Boolean automatedEntry;
    private boolean revisionCommentSet = false;
    // Only for internal use (persistence transient)
    private Integer oldVersionNum;

    @Type(type = "sourceType")
    public TranslationSourceType getSourceType() {
        return sourceType;
    }

    public void setRevisionComment(String revisionComment) {
        this.revisionComment = revisionComment;
        revisionCommentSet = true;
    }

    @Transient
    boolean isRevisionCommentSet() {
        return revisionCommentSet;
    }

    // Only for internal use (persistence transient)
    private HTextFlowTargetHistory initialState;

    public HTextFlowTarget(HTextFlow textFlow, @Nonnull HLocale locale) {
        this.locale = locale;
        this.textFlow = textFlow;
        this.textFlowRevision = textFlow.getRevision();
    }
    // TODO PERF @NaturalId(mutable=false) for better criteria caching

    @NaturalId
    @ManyToOne(optional = false)
    @JoinColumn(name = "locale", nullable = false, updatable = false)
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = LocaleIdBridge.class)
    @Nonnull
    public HLocale getLocale() {
        return locale;
    }

    @Transient
    @Override
    @Nonnull
    public LocaleId getLocaleId() {
        return locale.getLocaleId();
    }

    @NotNull
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = ContentStateBridge.class)
    @Override
    @Nonnull
    public ContentState getState() {
        return state;
    }

    public void setState(@Nonnull ContentState newState) {
        state = newState;
    }

    @NotNull
    @Column(name = "tf_revision")
    @Override
    public Integer getTextFlowRevision() {
        return textFlowRevision;
    }

    @ManyToOne(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by_id", nullable = true)
    @Override
    public HPerson getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(HPerson date) {
        lastModifiedBy = date;
    }

    @Override
    @ManyToOne(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinColumn(name = "translated_by_id", nullable = true)
    public HPerson getTranslator() {
        return translator;
    }

    @Override
    @ManyToOne(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id", nullable = true)
    public HPerson getReviewer() {
        return reviewer;
    }

    public boolean hasReviewer() {
        return reviewer != null;
    }
    // TODO PERF @NaturalId(mutable=false) for better criteria caching
    // @Field(index = Index.UN_TOKENIZED)
    // @FieldBridge(impl = ContainingWorkspaceBridge.class)

    @NaturalId
    @ManyToOne
    @JoinColumn(name = "tf_id")
    @IndexedEmbedded
    public HTextFlow getTextFlow() {
        return textFlow;
    }

    /**
     * As of release 1.6, replaced by {@link #getContents()}
     *
     * @return
     */
    @Deprecated
    @Transient
    public String getContent() {
        if (this.getContents().size() > 0) {
            return this.getContents().get(0);
        }
        return null;
    }

    @Deprecated
    @Transient
    public void setContent(String content) {
        this.setContents(Arrays.asList(content));
    }

    @Type(type = "entityType")
    public EntityType getCopiedEntityType() {
        return copiedEntityType;
    }
    // TODO extend HTextContainer and remove this

    @Override
    @Transient
    @Field(name = IndexFieldLabels.CONTENT,
            bridge = @FieldBridge(impl = StringListBridge.class,
                    params = { @Parameter(name = "case", value = "fold"),
                            @Parameter(name = "ngrams", value = "multisize") }))
    @AnalyzerDiscriminator(impl = TextContainerAnalyzerDiscriminator.class)
    public List<String> getContents() {
        List<String> contents = new ArrayList<String>();
        boolean populating = false;
        for (int i = MAX_PLURALS - 1; i >= 0; i--) {
            String c = this.getContent(i);
            if (c != null) {
                populating = true;
            }
            if (populating) {
                contents.add(0, c);
            }
        }
        return contents;
    }

    public void setContents(List<String> contents) {
        if (!Objects.equal(contents, this.getContents())) {
            for (int i = 0; i < contents.size(); i++) {
                this.setContent(i, contents.get(i));
            }
        }
    }

    private String getContent(int idx) {
        switch (idx) {
        case 0:
            return content0;

        case 1:
            return content1;

        case 2:
            return content2;

        case 3:
            return content3;

        case 4:
            return content4;

        case 5:
            return content5;

        default:
            throw new RuntimeException("Invalid Content index: " + idx);

        }
    }

    private void setContent(int idx, String content) {
        switch (idx) {
        case 0:
            content0 = content;
            break;

        case 1:
            content1 = content;
            break;

        case 2:
            content2 = content;
            break;

        case 3:
            content3 = content;
            break;

        case 4:
            content4 = content;
            break;

        case 5:
            content5 = content;
            break;

        default:
            throw new RuntimeException("Invalid Content index: " + idx);

        }
    }

    protected String getContent0() {
        return content0;
    }

    protected String getContent1() {
        return content1;
    }

    protected String getContent2() {
        return content2;
    }

    protected String getContent3() {
        return content3;
    }

    protected String getContent4() {
        return content4;
    }

    protected String getContent5() {
        return content5;
    }

    public void setContents(String... contents) {
        this.setContents(Arrays.asList(contents));
    }

    @OneToOne(optional = true, fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "comment_id")
    public HSimpleComment getComment() {
        return comment;
    }

    @OneToMany(cascade = { CascadeType.REMOVE, CascadeType.MERGE,
            CascadeType.PERSIST }, mappedBy = "textFlowTarget")
    @MapKey(name = "versionNum")
    public Map<Integer, HTextFlowTargetHistory> getHistory() {
        if (this.history == null) {
            this.history = new HashMap<Integer, HTextFlowTargetHistory>();
        }
        return history;
    }

    @OneToMany(cascade = { CascadeType.REMOVE, CascadeType.MERGE,
            CascadeType.PERSIST }, mappedBy = "textFlowTarget")
    public List<HTextFlowTargetReviewComment> getReviewComments() {
        if (reviewComments == null) {
            reviewComments = Lists.newArrayList();
        }
        return reviewComments;
    }

    public HTextFlowTargetReviewComment addReviewComment(String comment,
            HPerson commenter) {
        HTextFlowTargetReviewComment reviewComment =
                new HTextFlowTargetReviewComment(this, comment, commenter);
        getReviewComments().add(reviewComment);
        return reviewComment;
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this)
                .add("contents", getContents()).add("locale", getLocale())
                .add("state", getState()).add("comment", getComment());
        if (getTextFlow() == null) {
            return helper.toString();
        }
        return helper.add("textFlow", getTextFlow().getContents()).toString();
    }

    @Transient
    public void clear() {
        setContents(null, null, null, null, null, null);
        setState(ContentState.New);
        setComment(null);
        setLastModifiedBy(null);
        setTranslator(null);
        setReviewer(null);
        setRevisionComment(null);
        setSourceType(null);
        setCopiedEntityId(null);
        setCopiedEntityType(null);
    }

    protected boolean logPersistence() {
        return false;
    }

    @Override
    @Transient
    public EntityType getEntityType() {
        return EntityType.HTexFlowTarget;
    }

    public static class EntityListener {

        @PreUpdate
        private void preUpdate(HTextFlowTarget tft) {
            // insert history if this has changed from its initial state
            if (tft.initialState != null && tft.initialState.hasChanged(tft)) {
                if (tft.initialState.getSourceType() == null) {
                    tft.initialState
                            .setSourceType(TranslationSourceType.UNKNOWN);
                }
                tft.initialState.setAutomatedEntry(
                        tft.initialState.getSourceType().isAutomatedEntry());
                tft.getHistory().put(tft.oldVersionNum, tft.initialState);
                if (!tft.isRevisionCommentSet()) {
                    tft.setRevisionComment(null);
                }
            }
            setAutomatedEntry(tft);
        }

        @PrePersist
        private void prePersist(HTextFlowTarget tft) {
            setAutomatedEntry(tft);
        }

        private void setAutomatedEntry(HTextFlowTarget tft) {
            if (tft.getSourceType() == null) {
                tft.setSourceType(TranslationSourceType.UNKNOWN);
            }
            tft.setAutomatedEntry(tft.getSourceType().isAutomatedEntry());
        }

        @PostUpdate
        @PostPersist
        @PostLoad
        private void updateInternalHistory(HTextFlowTarget tft) {
            tft.oldVersionNum = tft.getVersionNum();
            tft.initialState = new HTextFlowTargetHistory(tft);
        }
    }

    public void setTextFlow(final HTextFlow textFlow) {
        this.textFlow = textFlow;
    }

    public void setLocale(@Nonnull final HLocale locale) {
        if (locale == null) {
            throw new NullPointerException("locale");
        }
        this.locale = locale;
    }

    public void setContent0(final String content0) {
        this.content0 = content0;
    }

    public void setContent1(final String content1) {
        this.content1 = content1;
    }

    public void setContent2(final String content2) {
        this.content2 = content2;
    }

    public void setContent3(final String content3) {
        this.content3 = content3;
    }

    public void setContent4(final String content4) {
        this.content4 = content4;
    }

    public void setContent5(final String content5) {
        this.content5 = content5;
    }

    public void setTextFlowRevision(final Integer textFlowRevision) {
        this.textFlowRevision = textFlowRevision;
    }

    public void setTranslator(final HPerson translator) {
        this.translator = translator;
    }

    public void setReviewer(final HPerson reviewer) {
        this.reviewer = reviewer;
    }

    public void setComment(final HSimpleComment comment) {
        this.comment = comment;
    }

    public void setHistory(final Map<Integer, HTextFlowTargetHistory> history) {
        this.history = history;
    }

    public void setReviewComments(
            final List<HTextFlowTargetReviewComment> reviewComments) {
        this.reviewComments = reviewComments;
    }

    public void setCopiedEntityType(final EntityType copiedEntityType) {
        this.copiedEntityType = copiedEntityType;
    }

    public void setCopiedEntityId(final Long copiedEntityId) {
        this.copiedEntityId = copiedEntityId;
    }

    public void setSourceType(final TranslationSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public void setRevisionCommentSet(final boolean revisionCommentSet) {
        this.revisionCommentSet = revisionCommentSet;
    }

    public HTextFlowTarget() {
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof HTextFlowTarget))
            return false;
        final HTextFlowTarget other = (HTextFlowTarget) o;
        if (!other.canEqual((Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final Object this$textFlow = this.getTextFlow();
        final Object other$textFlow = other.getTextFlow();
        if (this$textFlow == null ? other$textFlow != null
                : !this$textFlow.equals(other$textFlow))
            return false;
        final Object this$locale = this.getLocale();
        final Object other$locale = other.getLocale();
        if (this$locale == null ? other$locale != null
                : !this$locale.equals(other$locale))
            return false;
        final Object this$content0 = this.getContent0();
        final Object other$content0 = other.getContent0();
        if (this$content0 == null ? other$content0 != null
                : !this$content0.equals(other$content0))
            return false;
        final Object this$content1 = this.getContent1();
        final Object other$content1 = other.getContent1();
        if (this$content1 == null ? other$content1 != null
                : !this$content1.equals(other$content1))
            return false;
        final Object this$content2 = this.getContent2();
        final Object other$content2 = other.getContent2();
        if (this$content2 == null ? other$content2 != null
                : !this$content2.equals(other$content2))
            return false;
        final Object this$content3 = this.getContent3();
        final Object other$content3 = other.getContent3();
        if (this$content3 == null ? other$content3 != null
                : !this$content3.equals(other$content3))
            return false;
        final Object this$content4 = this.getContent4();
        final Object other$content4 = other.getContent4();
        if (this$content4 == null ? other$content4 != null
                : !this$content4.equals(other$content4))
            return false;
        final Object this$content5 = this.getContent5();
        final Object other$content5 = other.getContent5();
        if (this$content5 == null ? other$content5 != null
                : !this$content5.equals(other$content5))
            return false;
        final Object this$state = this.getState();
        final Object other$state = other.getState();
        if (this$state == null ? other$state != null
                : !this$state.equals(other$state))
            return false;
        final Object this$textFlowRevision = this.getTextFlowRevision();
        final Object other$textFlowRevision = other.getTextFlowRevision();
        if (this$textFlowRevision == null ? other$textFlowRevision != null
                : !this$textFlowRevision.equals(other$textFlowRevision))
            return false;
        final Object this$lastModifiedBy = this.getLastModifiedBy();
        final Object other$lastModifiedBy = other.getLastModifiedBy();
        if (this$lastModifiedBy == null ? other$lastModifiedBy != null
                : !this$lastModifiedBy.equals(other$lastModifiedBy))
            return false;
        final Object this$translator = this.getTranslator();
        final Object other$translator = other.getTranslator();
        if (this$translator == null ? other$translator != null
                : !this$translator.equals(other$translator))
            return false;
        final Object this$reviewer = this.getReviewer();
        final Object other$reviewer = other.getReviewer();
        if (this$reviewer == null ? other$reviewer != null
                : !this$reviewer.equals(other$reviewer))
            return false;
        final Object this$comment = this.getComment();
        final Object other$comment = other.getComment();
        if (this$comment == null ? other$comment != null
                : !this$comment.equals(other$comment))
            return false;
        final Object this$history = this.getHistory();
        final Object other$history = other.getHistory();
        if (this$history == null ? other$history != null
                : !this$history.equals(other$history))
            return false;
        final Object this$reviewComments = this.getReviewComments();
        final Object other$reviewComments = other.getReviewComments();
        if (this$reviewComments == null ? other$reviewComments != null
                : !this$reviewComments.equals(other$reviewComments))
            return false;
        final Object this$revisionComment = this.getRevisionComment();
        final Object other$revisionComment = other.getRevisionComment();
        if (this$revisionComment == null ? other$revisionComment != null
                : !this$revisionComment.equals(other$revisionComment))
            return false;
        final Object this$copiedEntityType = this.getCopiedEntityType();
        final Object other$copiedEntityType = other.getCopiedEntityType();
        if (this$copiedEntityType == null ? other$copiedEntityType != null
                : !this$copiedEntityType.equals(other$copiedEntityType))
            return false;
        final Object this$copiedEntityId = this.getCopiedEntityId();
        final Object other$copiedEntityId = other.getCopiedEntityId();
        if (this$copiedEntityId == null ? other$copiedEntityId != null
                : !this$copiedEntityId.equals(other$copiedEntityId))
            return false;
        final Object this$sourceType = this.getSourceType();
        final Object other$sourceType = other.getSourceType();
        if (this$sourceType == null ? other$sourceType != null
                : !this$sourceType.equals(other$sourceType))
            return false;
        final Object this$automatedEntry = this.getAutomatedEntry();
        final Object other$automatedEntry = other.getAutomatedEntry();
        if (this$automatedEntry == null ? other$automatedEntry != null
                : !this$automatedEntry.equals(other$automatedEntry))
            return false;
        if (this.isRevisionCommentSet() != other.isRevisionCommentSet())
            return false;
        final Object this$oldVersionNum = this.oldVersionNum;
        final Object other$oldVersionNum = other.oldVersionNum;
        if (this$oldVersionNum == null ? other$oldVersionNum != null
                : !this$oldVersionNum.equals(other$oldVersionNum))
            return false;
        final Object this$initialState = this.initialState;
        final Object other$initialState = other.initialState;
        if (this$initialState == null ? other$initialState != null
                : !this$initialState.equals(other$initialState))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof HTextFlowTarget;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final Object $textFlow = this.getTextFlow();
        result = result * PRIME
                + ($textFlow == null ? 43 : $textFlow.hashCode());
        final Object $locale = this.getLocale();
        result = result * PRIME + ($locale == null ? 43 : $locale.hashCode());
        final Object $content0 = this.getContent0();
        result = result * PRIME
                + ($content0 == null ? 43 : $content0.hashCode());
        final Object $content1 = this.getContent1();
        result = result * PRIME
                + ($content1 == null ? 43 : $content1.hashCode());
        final Object $content2 = this.getContent2();
        result = result * PRIME
                + ($content2 == null ? 43 : $content2.hashCode());
        final Object $content3 = this.getContent3();
        result = result * PRIME
                + ($content3 == null ? 43 : $content3.hashCode());
        final Object $content4 = this.getContent4();
        result = result * PRIME
                + ($content4 == null ? 43 : $content4.hashCode());
        final Object $content5 = this.getContent5();
        result = result * PRIME
                + ($content5 == null ? 43 : $content5.hashCode());
        final Object $state = this.getState();
        result = result * PRIME + ($state == null ? 43 : $state.hashCode());
        final Object $textFlowRevision = this.getTextFlowRevision();
        result = result * PRIME + ($textFlowRevision == null ? 43
                : $textFlowRevision.hashCode());
        final Object $lastModifiedBy = this.getLastModifiedBy();
        result = result * PRIME
                + ($lastModifiedBy == null ? 43 : $lastModifiedBy.hashCode());
        final Object $translator = this.getTranslator();
        result = result * PRIME
                + ($translator == null ? 43 : $translator.hashCode());
        final Object $reviewer = this.getReviewer();
        result = result * PRIME
                + ($reviewer == null ? 43 : $reviewer.hashCode());
        final Object $comment = this.getComment();
        result = result * PRIME + ($comment == null ? 43 : $comment.hashCode());
        final Object $history = this.getHistory();
        result = result * PRIME + ($history == null ? 43 : $history.hashCode());
        final Object $reviewComments = this.getReviewComments();
        result = result * PRIME
                + ($reviewComments == null ? 43 : $reviewComments.hashCode());
        final Object $revisionComment = this.getRevisionComment();
        result = result * PRIME
                + ($revisionComment == null ? 43 : $revisionComment.hashCode());
        final Object $copiedEntityType = this.getCopiedEntityType();
        result = result * PRIME + ($copiedEntityType == null ? 43
                : $copiedEntityType.hashCode());
        final Object $copiedEntityId = this.getCopiedEntityId();
        result = result * PRIME
                + ($copiedEntityId == null ? 43 : $copiedEntityId.hashCode());
        final Object $sourceType = this.getSourceType();
        result = result * PRIME
                + ($sourceType == null ? 43 : $sourceType.hashCode());
        final Object $automatedEntry = this.getAutomatedEntry();
        result = result * PRIME
                + ($automatedEntry == null ? 43 : $automatedEntry.hashCode());
        result = result * PRIME + (this.isRevisionCommentSet() ? 79 : 97);
        final Object $oldVersionNum = this.oldVersionNum;
        result = result * PRIME
                + ($oldVersionNum == null ? 43 : $oldVersionNum.hashCode());
        final Object $initialState = this.initialState;
        result = result * PRIME
                + ($initialState == null ? 43 : $initialState.hashCode());
        return result;
    }

    public String getRevisionComment() {
        return this.revisionComment;
    }

    public Long getCopiedEntityId() {
        return this.copiedEntityId;
    }

    public Boolean getAutomatedEntry() {
        return this.automatedEntry;
    }

    private void setAutomatedEntry(final Boolean automatedEntry) {
        this.automatedEntry = automatedEntry;
    }

    private void setOldVersionNum(final Integer oldVersionNum) {
        this.oldVersionNum = oldVersionNum;
    }

    private void setInitialState(final HTextFlowTargetHistory initialState) {
        this.initialState = initialState;
    }
}
