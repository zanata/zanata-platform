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
import java.util.Collection;
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
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Represents a flow of source text that should be processed as a stand-alone
 * structural unit.
 * 
 * @see org.zanata.rest.dto.resource.TextFlow
 * @author Asgeir Frimannsson <asgeirf@redhat.com>
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Setter
@NoArgsConstructor
@ToString(of = {"resId", "revision", "comment", "obsolete"})
@Slf4j
public class HTextFlow extends HTextContainer implements Serializable, ITextFlowHistory, HasSimpleComment, HasContents, ITextFlow
{
   private static final long serialVersionUID = 3023080107971905435L;

   private Long id;

   private Integer revision = 1;

   private String resId;

   @Setter(AccessLevel.PROTECTED)
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
   @Setter(AccessLevel.PRIVATE)
   private Integer oldRevision;
   
   // Only for internal use (persistence transient)
   @Setter(AccessLevel.PRIVATE)
   private HTextFlowHistory initialState;

   public HTextFlow(HDocument document, String resId, String content)
   {
      setDocument(document);
      setResId(resId);
      setContents(content);
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

   @Transient
   @Override
   public LocaleId getLocale()
   {
      return getDocument().getSourceLocaleId();
   }

   // we can't use @NotNull because the position isn't set until the object has
   // been persisted
   @Column(insertable = false, updatable = false, nullable = false)
   // @Column(insertable=false, updatable=false)
   @Override
   public Integer getPos()
   {
      return pos;
   }

   @Transient
   @Override
   public String getQualifiedId()
   {
      HDocument doc = getDocument();
      HProjectIteration iter = doc.getProjectIteration();
      HProject proj = iter.getProject();
      return proj.getSlug()+":"+iter.getSlug()+":"+doc.getDocId()+":"+getResId();
   }

   // TODO make this case sensitive
   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @Size(max = 255)
   @NotEmpty
   public String getResId()
   {
      return resId;
   }

   /**
    * @return whether this message supports plurals
    */
   public boolean isPlural()
   {
      return plural;
   }

   @NotNull
   @Override
   public Integer getRevision()
   {
      return revision;
   }

   @Override
   public boolean isObsolete()
   {
      return obsolete;
   }

   /**
    * Caller must ensure that textFlow is in document.textFlows if and only if
    * obsolete = false
    * 
    * @param obsolete
    */
   public void setObsolete(boolean obsolete)
   {
      this.obsolete = obsolete;
   }

   @ManyToOne
   @JoinColumn(name = "document_id", insertable = false, updatable = false, nullable = false)
   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @AccessType("field")
   @Field(analyze = Analyze.NO)
   @FieldBridge(impl = ContainingWorkspaceBridge.class)
   public HDocument getDocument()
   {
      return document;
   }

   public void setDocument(HDocument document)
   {
      if (!Objects.equal(this.document, document))
      {
         this.document = document;
         updateWordCount();
      }
   }

   // TODO use orphanRemoval=true: requires JPA 2.0
   @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
   @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
   @JoinColumn(name = "comment_id")
   public HSimpleComment getComment()
   {
      return comment;
   }

   @Override
   @NotEmpty
   @Transient
   public List<String> getContents()
   {
      List<String> contents = new ArrayList<String>();
      boolean populating = false;
      for( int i = MAX_PLURALS-1; i >= 0; i-- )
      {
         String c = this.getContent(i);
         if( c != null )
         {
            populating = true;
         }

         if( populating )
         {
            contents.add(0, c);
         }
      }
      return contents;
   }

   public void setContents(List<String> newContents)
   {
      if (!newContents.equals(this.getContents()))
      {
         for (int i = 0; i < MAX_PLURALS; i++)
         {
            String value = i < newContents.size() ? newContents.get(i) : null;
            this.setContent(i, value);
         }
         updateContentHash();
         updateWordCount();
      }
   }

   private String getContent(int idx)
   {
      switch (idx)
      {
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

   private void setContent(int idx, String content)
   {
      switch (idx)
      {
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

   protected String getContent0()
   {
      return content0;
   }

   protected String getContent1()
   {
      return content1;
   }

   protected String getContent2()
   {
      return content2;
   }

   protected String getContent3()
   {
      return content3;
   }

   protected String getContent4()
   {
      return content4;
   }

   protected String getContent5()
   {
      return content5;
   }

   @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "textFlow")
   @MapKey(name = "revision")
   public Map<Integer, HTextFlowHistory> getHistory()
   {
      if( this.history == null )
      {
         this.history = new HashMap<Integer, HTextFlowHistory>();
      }
      return history;
   }

   @OneToMany(cascade = CascadeType.ALL, mappedBy = "textFlow")
   @MapKeyColumn(name = "locale")
   @BatchSize(size = 10)
   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   public Map<Long, HTextFlowTarget> getTargets()
   {
      if (targets == null)
      {
         targets = new HashMap<Long, HTextFlowTarget>();
      }
      return targets;
   }

   @Override
   public ITextFlowTarget getTargetContents(LocaleId localeId)
   {
      // TODO performance: need efficient way to look up a target by LocaleId
      Collection<HTextFlowTarget> targets = getTargets().values();
      for (HTextFlowTarget tft : targets)
      {
         if (tft.getLocaleId().equals(localeId))
            return tft;
      }
      return null;
   }

   @Transient
   @Override
   public Iterable<ITextFlowTarget> getAllTargetContents()
   {
      return ImmutableList.<ITextFlowTarget>copyOf(getTargets().values());
   }

   @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
   public HPotEntryData getPotEntryData()
   {
      return potEntryData;
   }

   @NotNull
   public Long getWordCount()
   {
      return wordCount;
   }

   // this method is private because setContent(), and only setContent(), should
   // be setting wordCount
   private void setWordCount(Long wordCount)
   {
      this.wordCount = wordCount;
   }
   
   public String getContentHash()
   {
      return contentHash;
   }
   
   // this method is private because setContent(), and only setContent(), should
   // be setting the contentHash
   private void setContentHash(String contentHash)
   {
      this.contentHash = contentHash;
   }

   private void updateWordCount()
   {
      if (document == null)
      {
         // come back when the not-null constraints are satisfied!
         return;
      }
      String locale = toBCP47(document.getLocale());
      // TODO strip (eg) HTML tags before counting words. Needs more metadata
      // about the content type.
      long count = 0;
      for( String content : this.getContents() )
      {
         count += OkapiUtil.countWords(content, locale);
      }
      setWordCount(count);
   }
   
   private void updateContentHash()
   {
      String contents = StringUtil.concat(getContents(), '|');
      this.setContentHash(HashUtil.generateHash(contents));
   }

   private String toBCP47(HLocale hLocale)
   {
      HLocale docLocale = document.getLocale();
      if (docLocale == null)
      {
         // *should* only happen in tests
         log.warn("null locale, assuming 'en'");
         return "en";
      }
      LocaleId docLocaleId = docLocale.getLocaleId();
      return docLocaleId.getId();
   }
   
   @PreUpdate
   private void preUpdate()
   {
      if( !this.revision.equals(this.oldRevision) )
      {
         // there is an initial state
         if( this.initialState != null )
         {
            this.getHistory().put(this.oldRevision, this.initialState);
         }
         if (!isPlural())
         {
            // if plural form has changed, we need to clear out obsolete contents
            setContents(content0);
         }
      }
   }
   
   @PostUpdate
   @PostPersist
   @PostLoad
   private void updateInternalHistory()
   {
      this.oldRevision = this.revision;
      this.initialState = new HTextFlowHistory(this);
   }

}
