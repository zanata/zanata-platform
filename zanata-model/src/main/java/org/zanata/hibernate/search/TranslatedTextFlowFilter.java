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
package org.zanata.hibernate.search;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate Search filter for translated text flows. This implementation accepts an
 * {@link org.apache.lucene.util.OpenBitSet} with the Text Flow ids that are translated
 * in a particular language.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public class TranslatedTextFlowFilter extends Filter
{
   private OpenBitSet translatedTextFlowBitSet;

   public TranslatedTextFlowFilter(OpenBitSet translatedTextFlowBitSet)
   {
      this.translatedTextFlowBitSet = translatedTextFlowBitSet;
   }

   @Override
   public DocIdSet getDocIdSet(IndexReader reader) throws IOException
   {
      OpenBitSet docIdSet = new OpenBitSet(reader.maxDoc());

      for( long i=0; (i = translatedTextFlowBitSet.nextSetBit(i)) >= 0; i++ )
      {
         Term term = new Term("id", Long.toString(i)); // bit is the same as the text flow id
         TermDocs termDocs = reader.termDocs(term);
         while( termDocs.next() ) // Should only be one
         {
            docIdSet.set(termDocs.doc());
         }
      }
      return docIdSet;
   }
}
