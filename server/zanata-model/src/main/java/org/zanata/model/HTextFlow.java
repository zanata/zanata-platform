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

import static org.zanata.util.ZanataUtil.*;

import java.io.Serializable;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.NaturalId;
import org.hibernate.search.annotations.FilterCacheModeType;
import org.hibernate.search.annotations.FullTextFilterDef;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.HasContents;
import org.zanata.common.LocaleId;
import org.zanata.hibernate.search.IdFilterFactory;
import org.zanata.model.po.HPotEntryData;
import org.zanata.util.HashUtil;
import org.zanata.util.OkapiUtil;
import org.zanata.util.StringUtil;

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
@Indexed
@FullTextFilterDef(
      name = "textFlowFilter",
      impl = IdFilterFactory.class,
      cache = FilterCacheModeType.INSTANCE_AND_DOCIDSETRESULTS)
@NamedQueries(@NamedQuery(
      name = "HTextFlow.findIdsWithTranslations",
      query = "SELECT tft.textFlow.id FROM HTextFlowTarget tft " +
            "WHERE tft.locale.localeId=:locale " +
            "AND tft.state=org.zanata.common.ContentState.Approved " +
            "AND tft.textFlow.document.projectIteration.status<>org.zanata.common.EntityStatus.OBSOLETE " +
            "AND tft.textFlow.document.projectIteration.project.status<>org.zanata.common.EntityStatus.OBSOLETE"
))
public class HTextFlow extends HTextContainer implements HasContents, HasSimpleComment, ITextFlowHistory, Serializable
{
   private static final Logger log = LoggerFactory.getLogger(HTextFlow.class);

   private static final long serialVersionUID = 3023080107971905435L;

   private Long id;

   private Integer revision = 1;

   private String resId;

   private Integer pos;

   private HDocument document;

   private boolean obsolete = false;

   private Map<HLocale, HTextFlowTarget> targets;

   public Map<Integer, HTextFlowHistory> history;

   private HSimpleComment comment;

   private HPotEntryData potEntryData;
   
   private Long wordCount;
   
   private String contentHash;

   public HTextFlow()
   {

   }

   public HTextFlow(HDocument document, String resId, String... content)
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

   // we can't use @NotNull because the position isn't set until the object has
   // been persisted
   @Column(insertable = false, updatable = false, nullable = false)
   // @Column(insertable=false, updatable=false)
   @Override
   public Integer getPos()
   {
      return pos;
   }

   public void setPos(Integer pos)
   {
      this.pos = pos;
   }

   // TODO make this case sensitive
   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @Length(max = 255)
   @NotEmpty
   public String getResId()
   {
      return resId;
   }

   public void setResId(String resId)
   {
      this.resId = resId;
   }

   @NotNull
   @Override
   public Integer getRevision()
   {
      return revision;
   }

   public void setRevision(Integer revision)
   {
      this.revision = revision;
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
   public HDocument getDocument()
   {
      return document;
   }

   public void setDocument(HDocument document)
   {
      if (!equal(this.document, document))
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

   public void setComment(HSimpleComment comment)
   {
      this.comment = comment;
   }

   @Override
   public void setContents(List<String> contents)
   {
      super.setContents(contents);
      // if this becomes expensive, consider an EntityListener to do this only if contents changed
      updateWordCount();
      updateContentHash();
   }

   @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "textFlow")
   @MapKey(name = "revision")
   public Map<Integer, HTextFlowHistory> getHistory()
   {
      return history;
   }

   public void setHistory(Map<Integer, HTextFlowHistory> history)
   {
      this.history = history;
   }

   @OneToMany(cascade = CascadeType.ALL, mappedBy = "textFlow")
   @MapKey(name = "locale")
   @BatchSize(size = 10)
   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   public Map<HLocale, HTextFlowTarget> getTargets()
   {
      if (targets == null)
         targets = new HashMap<HLocale, HTextFlowTarget>();
      return targets;
   }

   public void setTargets(Map<HLocale, HTextFlowTarget> targets)
   {
      this.targets = targets;
   }

   public void setPotEntryData(HPotEntryData potEntryData)
   {
      this.potEntryData = potEntryData;
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
      for (String content : getContents())
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

   /**
    * Used for debugging
    */
   @Override
   public String toString()
   {
      return "HTextFlow(" + "resId:" + getResId() + " contents:" + getContents() + " revision:" + getRevision() + " comment:" + getComment() + " obsolete:" + isObsolete() + ")";
   }

}
