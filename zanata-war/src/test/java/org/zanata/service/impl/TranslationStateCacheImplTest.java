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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.impl.TranslationStateCacheImpl.TranslatedDocumentKey;

import com.google.common.cache.CacheLoader;


@Test(groups = { "business-tests" })
public class TranslationStateCacheImplTest
{
   TranslationStateCacheImpl tsCache;
   @Mock
   private CacheLoader<LocaleId, TranslatedTextFlowFilter> filterLoader;
   @Mock
   private CacheLoader<LocaleId, OpenBitSet> bitsetLoader;
   @Mock
   private CacheLoader<TranslatedDocumentKey, Long> lastModifiedLoader;
   @Mock
   private TextFlowTargetDAO textFlowTargetDAO;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      tsCache = new TranslationStateCacheImpl(filterLoader, bitsetLoader, lastModifiedLoader, textFlowTargetDAO);
      tsCache.create();
   }

   @AfterMethod
   public void afterMethod()
   {
      tsCache.destroy();
   }

   public void testGetFilter() throws Exception
   {
      // Given:
      LocaleId locale = new LocaleId("TEST");
      Filter filter = new TranslatedTextFlowFilter(locale);

      // When:
      when(filterLoader.load(locale)).thenReturn((TranslatedTextFlowFilter) filter);
      Filter result1 = tsCache.getFilter(locale);
      Filter result2 = tsCache.getFilter(locale);

      // Then:
      verify(filterLoader).load(locale); // only load the value once
      assertThat(result1, is(sameInstance(filter)));
      assertThat(result2, is(sameInstance(filter)));
   }

   public void testGetTextFlowIds() throws Exception
   {
      // Given:
      LocaleId locale = new LocaleId("TEST");
      OpenBitSet bitset = new OpenBitSet();

      // When:
      when(bitsetLoader.load(locale)).thenReturn(bitset);
      OpenBitSet result1 = tsCache.getTranslatedTextFlowIds(locale);
      OpenBitSet result2 = tsCache.getTranslatedTextFlowIds(locale);

      // Then:
      verify(bitsetLoader).load(locale); // only load the value once
      assertThat(result1, is(sameInstance(bitset)));
      assertThat(result2, is(sameInstance(bitset)));
   }

   public void testGetLastModifiedTextFlowTarget() throws Exception
   {
      // Given:
      Long documentId = new Long("100");
      LocaleId testLocaleId = LocaleId.DE;
      TranslatedDocumentKey key = new TranslatedDocumentKey(documentId, testLocaleId);
      Long targetId = new Long(0);
      HTextFlowTarget target = new HTextFlowTarget();

      // When:
      when(lastModifiedLoader.load(key)).thenReturn(targetId);
      when(textFlowTargetDAO.findById(targetId, false)).thenReturn(target);
      
      HTextFlowTarget result1 = tsCache.getDocLastModifiedTextFlowTarget(documentId, testLocaleId);
      HTextFlowTarget result2 = tsCache.getDocLastModifiedTextFlowTarget(documentId, testLocaleId);

      // Then:
      verify(lastModifiedLoader).load(key); // only load the value once
      assertThat(result1, is(sameInstance(target)));
      assertThat(result2, is(sameInstance(target)));
   }
}
