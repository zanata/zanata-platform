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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.zanata.common.HasContents;
import org.zanata.hibernate.search.CaseInsensitiveNgramAnalyzer;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
abstract class HTextContainer implements HasContents, Serializable
{
   private static final long serialVersionUID = 1L;


   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = CaseInsensitiveNgramAnalyzer.class))
   @SuppressWarnings("unused")
   private String getContent0()
   {
      return getContentAtIndex(0);
   }

   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = CaseInsensitiveNgramAnalyzer.class))
   @SuppressWarnings("unused")
   private String getContent1()
   {
      return getContentAtIndex(1);
   }

   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = CaseInsensitiveNgramAnalyzer.class))
   @SuppressWarnings("unused")
   private String getContent2()
   {
      return getContentAtIndex(2);
   }

   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = CaseInsensitiveNgramAnalyzer.class))
   @SuppressWarnings("unused")
   private String getContent3()
   {
      return getContentAtIndex(3);
   }

   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = CaseInsensitiveNgramAnalyzer.class))
   @SuppressWarnings("unused")
   private String getContent4()
   {
      return getContentAtIndex(4);
   }

   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = CaseInsensitiveNgramAnalyzer.class))
   @SuppressWarnings("unused")
   private String getContent5()
   {
      return getContentAtIndex(5);
   }
   
   private String getContentAtIndex(int idx)
   {
      return getContents() != null && getContents().size() > idx ? getContents().get(idx) : null;
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
      return getContentAtIndex(0);
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

   @Override
   public void setContents(String... args)
   {
      setContents(new ArrayList<String>(Arrays.asList(args)));
   }

}
