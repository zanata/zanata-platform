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
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.zanata.common.ContentState;
import org.zanata.model.type.EntityType;
import org.zanata.model.type.EntityTypeType;
import org.zanata.model.type.TranslationSourceType;
import com.google.common.base.Objects;
import org.zanata.model.type.TranslationSourceTypeType;

@Entity
@Immutable
@NamedQueries({
        @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY + 1,
                query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount and contents[0] = :content0"),
        @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY + 2,
                query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount and contents[0] = :content0 and contents[1] = :content1"),
        @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY + 3,
                query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount and contents[0] = :content0 and contents[1] = :content1 and contents[2] = :content2"),
        @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY + 4,
                query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount and contents[0] = :content0 and contents[1] = :content1 and contents[2] = :content2 and contents[3] = :content3"),
        @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY + 5,
                query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount and contents[0] = :content0 and contents[1] = :content1 and contents[2] = :content2 and contents[3] = :content3 and contents[4] = :content4"),
        @NamedQuery(name = HTextFlowTargetHistory.QUERY_MATCHING_HISTORY + 6,
                query = "select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = :tft and size(t.contents) = :contentCount and contents[0] = :content0 and contents[1] = :content1 and contents[2] = :content2 and contents[3] = :content3 and contents[4] = :content4 and contents[5] = :content5") })
@TypeDefs({
        @TypeDef(name = "sourceType",
                typeClass = TranslationSourceTypeType.class),
        @TypeDef(name = "entityType", typeClass = EntityTypeType.class) })
@EntityListeners({ HTextFlowTargetHistory.EntityListener.class })
public class HTextFlowTargetHistory extends HTextContainer
        implements Serializable, ITextFlowTargetHistory {
    static final String QUERY_MATCHING_HISTORY =
            "HTextFlowTargetHistory.QUERY_MATCHING_HISTORY.";

    public static String getQueryNameMatchingHistory(int size) {
        return QUERY_MATCHING_HISTORY + size;
    }

    private static final long serialVersionUID = 1L;
    private Long id;
    private HTextFlowTarget textFlowTarget;
    private Integer versionNum;
    private List<String> contents;
    private Date lastChanged;
    private HPerson lastModifiedBy;
    private ContentState state;
    private Integer textFlowRevision;
    private HPerson translator;
    private HPerson reviewer;
    private EntityType copiedEntityType;
    private Long copiedEntityId;
    private TranslationSourceType sourceType;
    private Boolean automatedEntry;
    private String revisionComment;

    public HTextFlowTargetHistory() {
    }

    public HTextFlowTargetHistory(HTextFlowTarget target) {
        this.lastChanged = target.getLastChanged();
        this.lastModifiedBy = target.getLastModifiedBy();
        this.state = target.getState();
        this.textFlowRevision = target.getTextFlowRevision();
        this.textFlowTarget = target;
        this.versionNum = target.getVersionNum();
        this.translator = target.getTranslator();
        this.reviewer = target.getReviewer();
        this.setContents(target.getContents());
        this.revisionComment = target.getRevisionComment();
        this.automatedEntry = target.getAutomatedEntry();
        this.sourceType = target.getSourceType();
        this.copiedEntityId = target.getCopiedEntityId();
        this.copiedEntityType = target.getCopiedEntityType();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }
    // TODO PERF @NaturalId(mutable=false) for better criteria caching

    @NaturalId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id")
    public HTextFlowTarget getTextFlowTarget() {
        return textFlowTarget;
    }

    public void setTextFlowTarget(HTextFlowTarget textFlowTarget) {
        this.textFlowTarget = textFlowTarget;
    }
    // TODO PERF @NaturalId(mutable=false) for better criteria caching

    @Override
    @NaturalId
    public Integer getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    @Override
    @javax.persistence.Lob
    @AccessType("field")
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "HTextFlowTargetContentHistory",
            joinColumns = @JoinColumn(name = "text_flow_target_history_id"))
    @IndexColumn(name = "pos", nullable = false)
    @Column(name = "content", nullable = false)
    public List<String> getContents() {
        return contents;
    }

    public void setContents(List<String> contents) {
        this.contents = new ArrayList<String>(contents);
    }

    public Date getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by_id", nullable = true)
    @Override
    public HPerson getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(HPerson lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override
    public ContentState getState() {
        return state;
    }

    public void setState(ContentState state) {
        this.state = state;
    }

    @Override
    @Column(name = "tf_revision")
    public Integer getTextFlowRevision() {
        return textFlowRevision;
    }

    public void setTextFlowRevision(Integer textFlowRevision) {
        this.textFlowRevision = textFlowRevision;
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

    public void setTranslator(HPerson translator) {
        this.translator = translator;
    }

    public void setReviewer(HPerson reviewer) {
        this.reviewer = reviewer;
    }

    @Type(type = "sourceType")
    public TranslationSourceType getSourceType() {
        return sourceType;
    }

    @Type(type = "entityType")
    public EntityType getCopiedEntityType() {
        return copiedEntityType;
    }

    public static class EntityListener {

        @PreUpdate
        private void preUpdate(HTextFlowTargetHistory tfth) {
            setAutomatedEntry(tfth);
        }

        @PrePersist
        private void prePersist(HTextFlowTargetHistory tfth) {
            setAutomatedEntry(tfth);
        }

        private void setAutomatedEntry(HTextFlowTargetHistory tfth) {
            if (tfth.getSourceType() == null) {
                tfth.setSourceType(TranslationSourceType.UNKNOWN);
            }
            tfth.setAutomatedEntry(tfth.getSourceType().isAutomatedEntry());
        }
    }

    /**
     * Determines whether a Text Flow Target has changed when compared to this
     * history object.
     *
     * @param current
     *            The current Text Flow Target state.
     * @return True, if any of the Text Flow Target fields have changed from the
     *         state recorded in this History object. False, otherwise.
     */
    public boolean hasChanged(HTextFlowTarget current) {
        return !Objects.equal(current.getContents(), this.contents)
                || !Objects.equal(current.getLastChanged(), this.lastChanged)
                || !Objects.equal(current.getLastModifiedBy(),
                        this.lastModifiedBy)
                || !Objects.equal(current.getTranslator(), this.translator)
                || !Objects.equal(current.getReviewer(), this.reviewer)
                || !Objects.equal(current.getState(), this.state)
                || !Objects.equal(current.getTextFlowRevision(),
                        this.textFlowRevision)
                || !Objects.equal(current.getLastChanged(), this.lastChanged)
                || !Objects.equal(current.getTextFlow().getId(),
                        this.textFlowTarget.getId())
                || !Objects.equal(current.getVersionNum(), this.versionNum)
                || !Objects.equal(current.getRevisionComment(),
                        this.revisionComment)
                || !Objects.equal(current.getCopiedEntityId(),
                        this.copiedEntityId)
                || !Objects.equal(current.getCopiedEntityType(),
                        this.copiedEntityType);
    }

    public void setCopiedEntityType(final EntityType copiedEntityType) {
        this.copiedEntityType = copiedEntityType;
    }

    public Long getCopiedEntityId() {
        return this.copiedEntityId;
    }

    public void setCopiedEntityId(final Long copiedEntityId) {
        this.copiedEntityId = copiedEntityId;
    }

    public void setSourceType(final TranslationSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Boolean getAutomatedEntry() {
        return this.automatedEntry;
    }

    public void setAutomatedEntry(final Boolean automatedEntry) {
        this.automatedEntry = automatedEntry;
    }

    public String getRevisionComment() {
        return this.revisionComment;
    }

    public void setRevisionComment(final String revisionComment) {
        this.revisionComment = revisionComment;
    }
}
