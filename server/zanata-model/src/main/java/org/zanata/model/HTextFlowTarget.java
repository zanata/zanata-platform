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

import static org.zanata.util.ListUtil.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.NotNull;
import org.zanata.common.ContentState;
import org.zanata.common.HasContents;
import org.zanata.hibernate.search.ContentStateBridge;
import org.zanata.hibernate.search.DefaultNgramAnalyzer;
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
//@Indexed
public class HTextFlowTarget extends ModelEntityBase implements HasContents, HasSimpleComment, ITextFlowTargetHistory, Serializable
{

   private static final long serialVersionUID = 302308010797605435L;

   private HTextFlow textFlow;
   private HLocale locale;

   private String content0, content1, content2, content3, content4, content5;

   @Transient
   private List<String> immutableContents;

   private ContentState state = ContentState.New;
   private Integer textFlowRevision;
   private HPerson lastModifiedBy;

   private HSimpleComment comment;

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

   /**
    * As of release 1.6, replaced by {@link #getContents()}
    * @return
    */
   @Override
   @Deprecated
   @Transient
   public String getContent()
   {
      return content0;
   }

   /**
    * As of release 1.6, replaced by {@link #setContents()}
    * @return
    */
   @Deprecated
   public void setContent(String content)
   {
      setContents(content);
   }

   @Transient
   public List<String> getContents()
   {
      if (immutableContents != null)
      {
         return immutableContents;
      }
      List<String> list = new ArrayList<String>(MAX_PLURALS);
      if (content0 != null)
      {
         list.add(content0);
         if (content1 != null)
         {
            list.add(content1);
            if (content2 != null)
            {
               list.add(content2);
               if (content3 != null)
               {
                  list.add(content3);
                  if (content4 != null)
                  {
                     list.add(content4);
                     if (content5 != null)
                     {
                        list.add(content5);
                     }
                  }
               }
            }
         }
      }
      this.immutableContents = Collections.unmodifiableList(list);
      return immutableContents;
   }

   @Override
   public void setContents(List<String> contents)
   {
      if (contents.size() > MAX_PLURALS)
      {
         throw new RuntimeException("too many plural forms");
      }
      this.immutableContents = Collections.unmodifiableList(new ArrayList<String>(contents));
      this.content0 = getElemOrNull(contents, 0);
      this.content1 = getElemOrNull(contents, 1);
      this.content2 = getElemOrNull(contents, 2);
      this.content3 = getElemOrNull(contents, 3);
      this.content4 = getElemOrNull(contents, 4);
      this.content5 = getElemOrNull(contents, 5);
   }

   @Override
   public void setContents(String... args)
   {
      setContents(Arrays.asList(args));
   }

   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = DefaultNgramAnalyzer.class))
   @SuppressWarnings("unused")
   private String getContent0()
   {
      return content0;
   }

   @SuppressWarnings("unused")
   private void setContent0(String content0)
   {
      this.content0 = content0;
   }

   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = DefaultNgramAnalyzer.class))
   @SuppressWarnings("unused")
   private String getContent1()
   {
      return content1;
   }

   @SuppressWarnings("unused")
   private void setContent1(String content1)
   {
      this.content1 = content1;
   }

   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = DefaultNgramAnalyzer.class))
   @SuppressWarnings("unused")
   private String getContent2()
   {
      return content2;
   }

   @SuppressWarnings("unused")
   private void setContent2(String content2)
   {
      this.content2 = content2;
   }

   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = DefaultNgramAnalyzer.class))
   @SuppressWarnings("unused")
   private String getContent3()
   {
      return content3;
   }

   @SuppressWarnings("unused")
   private void setContent3(String content3)
   {
      this.content3 = content3;
   }

   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = DefaultNgramAnalyzer.class))
   @SuppressWarnings("unused")
   private String getContent4()
   {
      return content4;
   }

   @SuppressWarnings("unused")
   private void setContent4(String content4)
   {
      this.content4 = content4;
   }

   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = DefaultNgramAnalyzer.class))
   @SuppressWarnings("unused")
   private String getContent5()
   {
      return content5;
   }

   @SuppressWarnings("unused")
   private void setContent5(String content5)
   {
      this.content5 = content5;
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

   /**
    * Used for debugging
    */
   @Override
   public String toString()
   {
      return "HTextFlowTarget(" + "contents:" + getContents() + " locale:" + getLocale() + " state:" + getState() + " comment:" + getComment() + " textflow:" + getTextFlow().getContents() + ")";
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
