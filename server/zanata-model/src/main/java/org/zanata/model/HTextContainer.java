/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.model;

import static org.zanata.util.ListUtil.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.zanata.common.HasContents;
import org.zanata.hibernate.search.DefaultNgramAnalyzer;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@MappedSuperclass
class HTextContainer implements HasContents, Serializable
{
   private static final long serialVersionUID = 1L;

   private String content0, content1, content2, content3, content4, content5;

   @Transient
   private List<String> immutableContents;

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

}
