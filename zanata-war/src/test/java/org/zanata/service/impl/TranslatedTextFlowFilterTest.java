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
package org.zanata.service.impl;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.service.TranslationStateCache;


@Test(groups = { "business-tests" })
public class TranslatedTextFlowFilterTest
{
   private static final int MAX_TFID = 10;
   // for this test, docIDs are equal to 10 minus the TF ID
   private static final int DOCID4 = MAX_TFID-4;
   private static final int DOCID3 = MAX_TFID-3;
   private LocaleId locale = new LocaleId("TEST");
   @Mock
   private TranslationStateCacheImpl translationStateCache;
   @Mock
   private IndexReader indexReader;
   @Mock
   private TermDocs term3Docs;
   @Mock
   private TermDocs term4Docs;
   private TranslatedTextFlowFilter filter;

   @SuppressWarnings("serial")
   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      filter = new TranslatedTextFlowFilter(locale)
      {
         @Override
         protected TranslationStateCache getTranslationStateCache()
         {
            return translationStateCache;
         }
      };
   }

   public void testGetFilter() throws Exception
   {
      // Given:
      OpenBitSet textFlowIdSet = new OpenBitSet(MAX_TFID);
      textFlowIdSet.set(3);
      textFlowIdSet.set(4);
      OpenBitSet docIdSet = new OpenBitSet(MAX_TFID);
      docIdSet.set(DOCID3);
      docIdSet.set(DOCID4);
      Term term3 = new Term("id", "3");
      Term term4 = new Term("id", "4");

      // When:
      when(translationStateCache.getTranslatedTextFlowIds(locale)).thenReturn(textFlowIdSet);
      when(indexReader.termDocs(term3)).thenReturn(term3Docs);
      when(term3Docs.next()).thenReturn(true, false);
      when(term3Docs.doc()).thenReturn(DOCID3);
      when(indexReader.termDocs(term4)).thenReturn(term4Docs);
      when(term4Docs.next()).thenReturn(true, false);
      when(term4Docs.doc()).thenReturn(DOCID4);
      DocIdSet result1 = filter.getDocIdSet(indexReader);
      DocIdSet result2 = filter.getDocIdSet(indexReader);

      // Then:
      assertThat(result1, is(equalTo((DocIdSet) docIdSet)));
      assertThat(result2, is(sameInstance(result1)));
   }

}
