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
import java.util.List;

import javax.persistence.Transient;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Parameter;
import org.zanata.common.HasContents;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.hibernate.search.StringListBridge;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
abstract class HTextContainer implements HasContents, Serializable
{
   private static final long serialVersionUID = 1L;

   @SuppressWarnings("unused")
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
   private List<String> getContentsToIndex()
   {
      return getContents();
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
      return getContents() != null && getContents().size() > 0 ? getContents().get(0) : null;
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
