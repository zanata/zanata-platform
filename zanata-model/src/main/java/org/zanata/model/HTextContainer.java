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

import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.NGramTokenizerFactory;
import org.apache.solr.analysis.StandardFilterFactory;
import org.apache.solr.analysis.StandardTokenizerFactory;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.AnalyzerDefs;
import org.hibernate.search.annotations.AnalyzerDiscriminator;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.zanata.common.HasContents;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.hibernate.search.StringListBridge;
import org.zanata.hibernate.search.TextContainerAnalyzerDiscriminator;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@AnalyzerDefs({
      @AnalyzerDef(name = "StandardAnalyzer",
            tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
            filters = {
                  @TokenFilterDef(factory = StandardFilterFactory.class),
                  @TokenFilterDef(factory = LowerCaseFilterFactory.class)
                  //@TokenFilterDef(factory = StopFilterFactory.class)
            }
      ),
      @AnalyzerDef(name = "UnigramAnalyzer",
            tokenizer = @TokenizerDef(factory = NGramTokenizerFactory.class,
                                      params = {@Parameter(name = "minGramSize", value = "1"),
                                                @Parameter(name = "maxGramSize", value = "1")
                                               }
                                     ),
            filters = {
                  @TokenFilterDef(factory = LowerCaseFilterFactory.class)
            }
      )
})
abstract class HTextContainer implements HasContents, Serializable
{
   private static final long serialVersionUID = 1L;

   @SuppressWarnings("unused")
   @Field(name=IndexFieldLabels.CONTENT,
          bridge = @FieldBridge(impl = StringListBridge.class))
   @AnalyzerDiscriminator(impl = TextContainerAnalyzerDiscriminator.class)
   private List<String> getContentsToIndex()
   {
      return getContents();
   }

   /**
    * As of release 1.6, replaced by {@link #getContents()}
    * @return
    */
   @Deprecated
   @Transient
   public String getContent()
   {
      return getContents() != null && getContents().size() > 0 ? getContents().get(0) : null;
   }

   /**
    * As of release 1.6, replaced by {@link #setContents(String...)}
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
