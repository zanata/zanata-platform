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
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.zanata.common.ContentState;
import org.zanata.hibernate.search.ContentStateBridge;
import org.zanata.hibernate.search.LocaleIdBridge;

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
               query = "select tft, tfExample, max(tft.lastChanged) " +
               		  "from HTextFlowTarget tft, HTextFlow tfExample " +
                       "left join fetch tft.textFlow " +
                       "where " +
                       "tfExample.resId = tft.textFlow.resId " +
                       "and tfExample.document = :document " +
                       "and tfExample.contentHash = tft.textFlow.contentHash " +
                       "and tft.textFlow.document.docId = :docId " +
                       "and tft.locale = :locale " +
                       "and tft.state = :state " +
                       "group by tft.textFlow.contentHash")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HTextFlowTarget extends ModelEntityBase implements ITextFlowTargetHistory, HasSimpleComment
{

   private static final long serialVersionUID = 302308010797605435L;

   private HTextFlow textFlow;
   private HLocale locale;

   private List<String> contents;
   private ContentState state = ContentState.New;
   private Integer textFlowRevision;
   private HPerson lastModifiedBy;

   private HSimpleComment comment;
   
   public Map<Integer, HTextFlowTargetHistory> history;
   
   // Only for internal use (persistence transient)
   private Integer oldVersionNum;
   
   // Only for internal use (persistence transient) 
   private HTextFlowTargetHistory initialState;
   

   public HTextFlowTarget()
   {
   }

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

   public void setLocale(HLocale locale)
   {
      this.locale = locale;
   }

   @NotNull
   @Field(index = Index.UN_TOKENIZED)
   @FieldBridge(impl = ContentStateBridge.class)
   @Override
   public ContentState getState()
   {
      return state;
   }

   public void setState(ContentState state)
   {
      this.state = state;
   }

   @NotNull
   @Column(name = "tf_revision")
   @Override
   public Integer getTextFlowRevision()
   {
      return textFlowRevision;
   }

   public void setTextFlowRevision(Integer textFlowRevision)
   {
      this.textFlowRevision = textFlowRevision;
   }

   @ManyToOne(cascade = { CascadeType.MERGE })
   @JoinColumn(name = "last_modified_by_id", nullable = true)
   @Override
   public HPerson getLastModifiedBy()
   {
      return lastModifiedBy;
   }

   public void setLastModifiedBy(HPerson lastModifiedBy)
   {
      this.lastModifiedBy = lastModifiedBy;
   }

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @ManyToOne
   @JoinColumn(name = "tf_id")
   @IndexedEmbedded(depth = 2)
   public HTextFlow getTextFlow()
   {
      return textFlow;
   }

   public void setTextFlow(HTextFlow textFlow)
   {
      this.textFlow = textFlow;
      // setResourceRevision(textFlow.getRevision());
   }

   @Deprecated
   @Transient
   public String getContent()
   {
      if( this.getContents().size() > 0 )
      {
         return this.getContents().get(0);
      }
      return null;
   }
   
   @Deprecated
   public void setContent( String content )
   {
      this.setContents( Arrays.asList(content) );
   }
   
   @Override
   @NotEmpty
   @Type(type = "text")
   @AccessType("field")
   @CollectionOfElements(fetch = FetchType.EAGER)
   @JoinTable(name = "HTextFlowTargetContent", 
      joinColumns = @JoinColumn(name = "text_flow_target_id")
   )
   @IndexColumn(name = "pos", nullable = false)
   @Column(name = "content", nullable = false)
   public List<String> getContents()
   {
      if( contents == null )
      {
         contents = new ArrayList<String>();
      }
      return contents;
   }

   public void setContents(List<String> contents)
   {
      this.contents = contents;
   }
   
   public void setContents(String ... contents)
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

   public void setComment(HSimpleComment comment)
   {
      this.comment = comment;
   }
   
   @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "textFlowTarget")
   @MapKey(name = "versionNum")
   public Map<Integer, HTextFlowTargetHistory> getHistory()
   {
      if( this.history == null )
      {
         this.history = new HashMap<Integer, HTextFlowTargetHistory>();
      }
      return history;
   }

   public void setHistory(Map<Integer, HTextFlowTargetHistory> history)
   {
      this.history = history;
   }
   
   @PreUpdate
   private void preUpdate()
   {
      // insert history if this has changed from its initial state
      if( this.initialState != null && this.initialState.hasChanged(this) )
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
   }

   /**
    * Used for debugging
    */
   @Override
   public String toString()
   {
      return "HTextFlowTarget(" + "content:" + getContent() + " locale:" + getLocale() + " state:" + getState() + " comment:" + getComment() + " textflow:" + getTextFlow().getContents() + ")";
   }

   @Transient
   public void clear()
   {
      setContents("");
      setState(ContentState.New);
      setComment(null);
      setLastModifiedBy(null);
   }

   protected boolean logPersistence()
   {
      return false;
   }

}
