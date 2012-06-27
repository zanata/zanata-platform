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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.validator.NotNull;
import org.zanata.common.ContentState;
import org.zanata.common.HasContents;
import org.zanata.hibernate.search.ContainingWorkspaceBridge;
import org.zanata.hibernate.search.ContentStateBridge;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.hibernate.search.LocaleIdBridge;
import org.zanata.hibernate.search.StringListBridge;

import com.google.common.base.Objects;

/**
 * Represents a flow of translated text that should be processed as a
 * stand-alone structural unit.
 * 
 * @see org.zanata.rest.dto.resource.TextFlowTarget
 * @author Asgeir Frimannsson <asgeirf@redhat.com>
 * 
 */
@Entity
@NamedQueries({
   @NamedQuery(name = "HTextFlowTarget.findLatestEquivalentTranslations",
               query = "select match, textFlow " +
                       "from HTextFlowTarget match, HTextFlow textFlow " +
                       "left join fetch match.textFlow " +
                       "where " +
                       "textFlow.document = :document " +
                       "and textFlow.contentHash = match.textFlow.contentHash " +
                       "and match.locale = :locale " +
                       "and match.state = :state " +
                       "and match.textFlow != textFlow " +       // Do not reuse its own translations
                       "order by textFlow.id, match.lastChanged desc")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Indexed
@Setter
@NoArgsConstructor
public class HTextFlowTarget extends ModelEntityBase implements HasContents, HasSimpleComment, ITextFlowTargetHistory, Serializable
{

   private static final long serialVersionUID = 302308010797605435L;

   private HTextFlow textFlow;
   private HLocale locale;

   private List<String> contents;
   private ContentState state = ContentState.New;
   private Integer textFlowRevision;
   private HPerson lastModifiedBy;

   private HSimpleComment comment;

   private Map<Integer, HTextFlowTargetHistory> history;

   // Only for internal use (persistence transient)
   @Setter(AccessLevel.PRIVATE)
   private Integer oldVersionNum;

   // Only for internal use (persistence transient)
   @Setter(AccessLevel.PRIVATE)
   private HTextFlowTargetHistory initialState;

   // Only for internal use (persistence transient)
   @Setter(AccessLevel.PRIVATE)
   private boolean lazyRelationsCopied = false;

   public HTextFlowTarget(HTextFlow textFlow, HLocale locale)
   {
      this.locale = locale;
      this.textFlow = textFlow;
      this.textFlowRevision = textFlow.getRevision();
   }

   @Id
   @GeneratedValue
   public Long getId()
   {
      return id;
   }

   protected void setId(Long id)
   {
      this.id = id;
   }

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @ManyToOne
   @JoinColumn(name = "locale", nullable = false)
   @Field(index = Index.UN_TOKENIZED)
   @FieldBridge(impl = LocaleIdBridge.class)
   public HLocale getLocale()
   {
      return locale;
   }

   @NotNull
   @Field(index = Index.UN_TOKENIZED)
   @FieldBridge(impl = ContentStateBridge.class)
   @Override
   public ContentState getState()
   {
      return state;
   }

   @NotNull
   @Column(name = "tf_revision")
   @Override
   public Integer getTextFlowRevision()
   {
      return textFlowRevision;
   }

   @ManyToOne(cascade = { CascadeType.MERGE })
   @JoinColumn(name = "last_modified_by_id", nullable = true)
   @Override
   public HPerson getLastModifiedBy()
   {
      return lastModifiedBy;
   }

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @ManyToOne
   @JoinColumn(name = "tf_id")
   @Field(index = Index.UN_TOKENIZED)
   @FieldBridge(impl = ContainingWorkspaceBridge.class)
   public HTextFlow getTextFlow()
   {
      return textFlow;
   }

   /**
    * As of release 1.6, replaced by {@link #getContents()}
    * 
    * @return
    */
   @Deprecated
   @Transient
   public String getContent()
   {
      if (this.getContents().size() > 0)
      {
         return this.getContents().get(0);
      }
      return null;
   }

   @Deprecated
   @Transient
   public void setContent(String content)
   {
      this.setContents(Arrays.asList(content));
   }

   @Override
   @Type(type = "text")
   @AccessType("field")
   @CollectionOfElements(fetch = FetchType.EAGER)
   @JoinTable(name = "HTextFlowTargetContent", 
              joinColumns = @JoinColumn(name = "text_flow_target_id")
   )
   @IndexColumn(name = "pos", nullable = false)
   @Column(name = "content", nullable = false)
   // TODO extend HTextContainer and remove this
   @Fields({
      @Field(name=IndexFieldLabels.CONTENT_CASE_FOLDED,
             index = Index.TOKENIZED,
             bridge = @FieldBridge(impl = StringListBridge.class,
                                   params = {@Parameter(name="case", value="fold"),
                                             @Parameter(name="ngrams", value="multisize")})),
      @Field(name = IndexFieldLabels.CONTENT_CASE_PRESERVED,
             index = Index.TOKENIZED,
             bridge = @FieldBridge(impl = StringListBridge.class,
                                   params = {@Parameter(name="case", value="preserve"),
                                             @Parameter(name="ngrams", value="multisize")}))
   })

   public List<String> getContents()
   {
      // Copy lazily loaded relations to the history object as this cannot be
      // done in the entity callbacks
      copyLazyLoadedRelationsToHistory();

      if (contents == null)
      {
         contents = new ArrayList<String>();
      }
      return contents;
   }

   public void setContents(List<String> contents)
   {
      // Copy lazily loaded relations to the history object as this cannot be
      // done in the entity callbacks
      copyLazyLoadedRelationsToHistory();

      this.contents = new ArrayList<String>(contents);
   }

   public void setContents(String... contents)
   {
      this.setContents(Arrays.asList(contents));
   }

   // TODO use orphanRemoval=true: requires JPA 2.0
   @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
   @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
   @JoinColumn(name = "comment_id")
   public HSimpleComment getComment()
   {
      return comment;
   }

   @OneToMany(cascade = { CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST }, mappedBy = "textFlowTarget")
   @MapKey(name = "versionNum")
   public Map<Integer, HTextFlowTargetHistory> getHistory()
   {
      if (this.history == null)
      {
         this.history = new HashMap<Integer, HTextFlowTargetHistory>();
      }
      return history;
   }

   @PreUpdate
   private void preUpdate()
   {
      // insert history if this has changed from its initial state
      if (this.initialState != null && this.initialState.hasChanged(this))
      {
         this.getHistory().put(this.oldVersionNum, this.initialState);
      }
   }

   @PostUpdate
   @PostPersist
   @PostLoad
   private void updateInternalHistory()
   {
      this.oldVersionNum = this.getVersionNum();
      this.initialState = new HTextFlowTargetHistory(this);
      this.lazyRelationsCopied = false;
   }

   /**
    * Copies all lazy loaded relations to the history object.
    */
   private void copyLazyLoadedRelationsToHistory()
   {
      if (this.initialState != null && this.initialState.getContents() == null && !this.lazyRelationsCopied)
      {
         if( this.contents != null )
         {
            this.initialState.setContents(this.contents);
         }
         this.lazyRelationsCopied = true;
      }
   }

   @Override
   public String toString()
   {
      return Objects.toStringHelper(this).
            add("contents", getContents()).
            add("locale", getLocale()).
            add("state", getState()).
            add("comment", getComment()).
            add("textFlow", getTextFlow().getContents()).
            toString();
   }
   @Transient
   public void clear()
   {
      setContents();
      setState(ContentState.New);
      setComment(null);
      setLastModifiedBy(null);
   }

   protected boolean logPersistence()
   {
      return false;
   }

}
