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
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.service.ValidationService;
import org.zanata.service.impl.TranslationStateCacheImpl.TranslatedDocumentKey;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.model.ValidationId;

import com.google.common.cache.CacheLoader;


@Test(groups = { "business-tests" })
public class TranslationStateCacheImplTest
{
   TranslationStateCacheImpl tsCache;
   @Mock
   private CacheLoader<TranslatedDocumentKey, DocumentStatus> docStatsLoader;
   @Mock
   private TextFlowTargetDAO textFlowTargetDAO;
   @Mock
   private CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader;
   @Mock
   private ValidationService validationServiceImpl;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      tsCache = new TranslationStateCacheImpl(docStatsLoader, targetValidationLoader, textFlowTargetDAO, validationServiceImpl);

      tsCache.create();
      tsCache.destroy();
      tsCache.create();
   }

   @AfterMethod
   public void afterMethod()
   {
      tsCache.destroy();
   }

   public void testGetLastModifiedTextFlowTarget() throws Exception
   {
      // Given:
      Long documentId = new Long("100");
      LocaleId testLocaleId = LocaleId.DE;
      TranslatedDocumentKey key = new TranslatedDocumentKey(documentId, testLocaleId);
      DocumentStatus docStats = new DocumentStatus(new DocumentId(documentId, ""), false, new Date(), "");

      // When:
      when(docStatsLoader.load(key)).thenReturn(docStats);
      
      DocumentStatus result1 = tsCache.getDocStats(documentId, testLocaleId);
      DocumentStatus result2 = tsCache.getDocStats(documentId, testLocaleId);

      // Then:
      verify(docStatsLoader).load(key); // only load the value once
      assertThat(result1, equalTo(docStats));
      assertThat(result2, equalTo(docStats));
   }

   public void testTextFlowTargetHasError() throws Exception
   {
      // Given:
      Long targetId = new Long("1000");
      ValidationId validationId = ValidationId.HTML_XML;
      Map<ValidationId, Boolean> map = new HashMap<ValidationId, Boolean>();

      // When:
      when(targetValidationLoader.load(targetId)).thenReturn(map);

      // Run:
      Boolean result = tsCache.textFlowTargetHasError(targetId, validationId);

      // Then:
      verify(targetValidationLoader).load(targetId); // only load the value once
      assertThat(result, equalTo(null));
   }

   public void testTextFlowTargetHasError2() throws Exception
   {
      // Given:
      Long targetId = new Long("1000");
      ValidationId validationId = ValidationId.HTML_XML;
      Map<ValidationId, Boolean> map = new HashMap<ValidationId, Boolean>();
      map.put(validationId, true);

      // When:
      when(targetValidationLoader.load(targetId)).thenReturn(map);

      // Run:
      Boolean result = tsCache.textFlowTargetHasError(targetId, validationId);

      // Then:
      verify(targetValidationLoader).load(targetId); // only load the value once
      assertThat(result, equalTo(true));
   }
}
