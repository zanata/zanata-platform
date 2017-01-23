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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.HasContents;
import org.zanata.common.LocaleId;
import org.zanata.hibernate.search.ContainingWorkspaceBridge;
import org.zanata.model.po.HPotEntryData;
import org.zanata.util.HashUtil;
import org.zanata.util.OkapiUtil;
import org.zanata.util.StringUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * Represents a flow of source text that should be processed as a stand-alone
 * structural unit.
 *
 * @see org.zanata.rest.dto.resource.TextFlow
 * @author Asgeir Frimannsson <asgeirf@redhat.com>
 */
@Entity
@EntityListeners({ HTextFlow.EntityListener.class })
@Cacheable
@NamedQueries({ @NamedQuery(name = HTextFlow.QUERY_GET_BY_DOC_AND_RES_ID_BATCH,
        query = "select distinct tf from HTextFlow tf left join fetch tf.targets tft left join fetch tft.history where tf.document = :document and tf.resId in (:resIds) and tf.obsolete = false") })
public class HTextFlow extends HTextContainer implements Serializable,
        ITextFlowHistory, HasSimpleComment, HasContents, ITextFlow {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(HTextFlow.class);
    public static final String QUERY_GET_BY_DOC_AND_RES_ID_BATCH =
            "HTextFlow.getByDocumentAndResIdBatch";
    private static final long serialVersionUID = 3023080107971905435L;
    private Long id;
    private Integer revision = 1;
    private String resId;
    private Integer pos;
    private HDocument document;
    private boolean obsolete = false;
    private Map<Long, HTextFlowTarget> targets;
    private Map<Integer, HTextFlowHistory> history;
    private HSimpleComment comment;
    private HPotEntryData potEntryData;
    private Long wordCount;
    private String contentHash;
    private boolean plural;
    private String content0;
    private String content1;
    private String content2;
    private String content3;
    private String content4;
    private String content5;
    // Only for internal use (persistence transient)
    private Integer oldRevision;
    // Only for internal use (persistence transient)
    private HTextFlowHistory initialState;

    public HTextFlow(HDocument document, String resId, String content) {
        setDocument(document);
        setResId(resId);
        setContents(content);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    @VisibleForTesting
    public void setId(Long id) {
        this.id = id;
    }

    @Transient
    @Override
    public LocaleId getLocale() {
        return getDocument().getSourceLocaleId();
    }
    // we can't use @NotNull because the position isn't set until the object has
    // been persisted
    // @Column(insertable=false, updatable=false)

    @Column(insertable = false, updatable = false, nullable = false)
    @Override
    public Integer getPos() {
        return pos;
    }

    @Transient
    @Override
    public String getQualifiedId() {
        HDocument doc = getDocument();
        HProjectIteration iter = doc.getProjectIteration();
        HProject proj = iter.getProject();
        return proj.getSlug() + ":" + iter.getSlug() + ":" + doc.getDocId()
                + ":" + getResId();
    }
    // TODO make this case sensitive
    // TODO PERF @NaturalId(mutable=false) for better criteria caching

    @NaturalId
    @Size(max = 255)
    @NotEmpty
    @Field(analyze = Analyze.NO)
    public String getResId() {
        return resId;
    }

    /**
     * @return whether this message supports plurals
     */
    public boolean isPlural() {
        return plural;
    }

    @NotNull
    @Override
    public Integer getRevision() {
        return revision;
    }

    @Override
    public boolean isObsolete() {
        return obsolete;
    }

    /**
     * Caller must ensure that textFlow is in document.textFlows if and only if
     * obsolete = false
     *
     * @param obsolete
     */
    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }
    // TODO PERF @NaturalId(mutable=false) for better criteria caching

    @ManyToOne
    @JoinColumn(name = "document_id", insertable = false, updatable = false,
            nullable = false)
    @NaturalId
    @AccessType("field")
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = ContainingWorkspaceBridge.class)
    public HDocument getDocument() {
        return document;
    }

    public void setDocument(HDocument document) {
        if (!Objects.equal(this.document, document)) {
            this.document = document;
            updateWordCount();
        }
    }
    // TODO use orphanRemoval=true: requires JPA 2.0

    @OneToOne(optional = true, fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "comment_id")
    public HSimpleComment getComment() {
        return comment;
    }

    @Override
    @NotEmpty
    @Transient
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

    public void setContents(List<String> newContents) {
        if (!newContents.equals(this.getContents())) {
            for (int i = 0; i < MAX_PLURALS; i++) {
                String value =
                        i < newContents.size() ? newContents.get(i) : null;
                this.setContent(i, value);
            }
            updateContentHash();
            updateWordCount();
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

    @OneToMany(cascade = { CascadeType.REMOVE, CascadeType.MERGE,
            CascadeType.PERSIST }, mappedBy = "textFlow")
    @MapKey(name = "revision")
    public Map<Integer, HTextFlowHistory> getHistory() {
        if (this.history == null) {
            this.history = new HashMap<Integer, HTextFlowHistory>();
        }
        return history;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "textFlow")
    @MapKeyColumn(name = "locale")
    @BatchSize(size = 10)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    public Map<Long, HTextFlowTarget> getTargets() {
        if (targets == null) {
            targets = new HashMap<Long, HTextFlowTarget>();
        }
        return targets;
    }

    @Override
    public ITextFlowTarget getTargetContents(LocaleId localeId) {
        // TODO performance: need efficient way to look up a target by LocaleId
        return getTargets().values().stream()
                .filter(tft -> tft.getLocaleId().equals(localeId)).findFirst()
                .orElse(null);
    }

    @Transient
    @Override
    public Iterable<ITextFlowTarget> getAllTargetContents() {
        return ImmutableList.<ITextFlowTarget> copyOf(getTargets().values());
    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
            optional = true)
    public HPotEntryData getPotEntryData() {
        return potEntryData;
    }

    @NotNull
    public Long getWordCount() {
        return wordCount;
    }
    // this method is private because setContent(), and only setContent(),
    // should
    // be setting wordCount

    private void setWordCount(Long wordCount) {
        this.wordCount = wordCount;
    }

    @Field(analyze = Analyze.NO)
    public String getContentHash() {
        return contentHash;
    }
    // this method is private because setContent(), and only setContent(),
    // should
    // be setting the contentHash

    private void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    private void updateWordCount() {
        if (document == null) {
            // come back when the not-null constraints are satisfied!
            return;
        }
        String locale = toBCP47(document.getLocale());
        // TODO strip (eg) HTML tags before counting words. Needs more metadata
        // about the content type.
        long count = this.getContents().stream()
                .mapToLong(s -> OkapiUtil.countWords(s, locale)).sum();
        setWordCount(count);
    }

    private void updateContentHash() {
        String contents = StringUtil.concat(getContents(), '|');
        this.setContentHash(HashUtil.generateHash(contents));
    }

    private static String toBCP47(HLocale docLocale) {
        if (docLocale == null) {
            // *should* only happen in tests
            log.warn("null locale, assuming \'en\'");
            return "en";
        }
        LocaleId docLocaleId = docLocale.getLocaleId();
        return docLocaleId.getId();
    }

    public static class EntityListener {

        @PreUpdate
        private void preUpdate(HTextFlow tf) {
            if (!tf.revision.equals(tf.oldRevision)) {
                // there is an initial state
                if (tf.initialState != null) {
                    tf.getHistory().put(tf.oldRevision, tf.initialState);
                }
                if (!tf.isPlural()) {
                    // if plural form has changed, we need to clear out obsolete
                    // contents
                    tf.setContents(tf.content0);
                }
            }
        }

        @PostUpdate
        @PostPersist
        @PostLoad
        private void updateInternalHistory(HTextFlow tf) {
            tf.oldRevision = tf.revision;
            tf.initialState = new HTextFlowHistory(tf);
        }
    }

    public void setRevision(final Integer revision) {
        this.revision = revision;
    }

    public void setResId(final String resId) {
        this.resId = resId;
    }

    public void setTargets(final Map<Long, HTextFlowTarget> targets) {
        this.targets = targets;
    }

    public void setHistory(final Map<Integer, HTextFlowHistory> history) {
        this.history = history;
    }

    public void setComment(final HSimpleComment comment) {
        this.comment = comment;
    }

    public void setPotEntryData(final HPotEntryData potEntryData) {
        this.potEntryData = potEntryData;
    }

    public void setPlural(final boolean plural) {
        this.plural = plural;
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

    public HTextFlow() {
    }

    @Override
    public String toString() {
        return "HTextFlow(revision=" + this.getRevision() + ", resId="
                + this.getResId() + ", obsolete=" + this.isObsolete()
                + ", comment=" + this.getComment() + ")";
    }

    protected void setPos(final Integer pos) {
        this.pos = pos;
    }

    private void setOldRevision(final Integer oldRevision) {
        this.oldRevision = oldRevision;
    }

    private void setInitialState(final HTextFlowHistory initialState) {
        this.initialState = initialState;
    }
}
