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

import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.cache.InfinispanTestCacheContainer;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.impl.TranslationStateCacheImpl.DocumentLocaleKey;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.model.ValidationId;

import com.google.common.cache.CacheLoader;

@Test(groups = { "business-tests" })
public class TranslationStateCacheImplTest {
    TranslationStateCacheImpl tsCache;
    @Mock
    private CacheLoader<DocumentLocaleKey, WordStatistic> docStatisticLoader;

    @Mock
    private CacheLoader<DocumentLocaleKey, DocumentStatus> docStatusLoader;
    @Mock
    private TextFlowTargetDAO textFlowTargetDAO;
    @Mock
    private CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader;

    private CacheContainer cacheContainer = new InfinispanTestCacheContainer();

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        cacheContainer.start();
        tsCache =
                new TranslationStateCacheImpl(docStatisticLoader,
                        docStatusLoader, targetValidationLoader);

        SeamAutowire seam = SeamAutowire.instance();
        seam.reset()
            .use("textFlowTargetDAO", textFlowTargetDAO)
            .use("cacheContainer", cacheContainer)
            .ignoreNonResolvable();
        tsCache = seam.autowire(tsCache);

        tsCache.create();
    }

    public void testGetLastModifiedTextFlowTarget() throws Exception {
        // Given:
        Long documentId = new Long("100");
        LocaleId testLocaleId = LocaleId.DE;
        TranslationStateCacheImpl.DocumentLocaleKey key =
                new DocumentLocaleKey(documentId, testLocaleId);
        DocumentStatus docStats =
                new DocumentStatus(new DocumentId(documentId, ""), new Date(),
                        "");

        // When:
        when(docStatusLoader.load(key)).thenReturn(docStats);

        DocumentStatus result1 =
                tsCache.getDocumentStatus(documentId, testLocaleId);
        DocumentStatus result2 =
                tsCache.getDocumentStatus(documentId, testLocaleId);

        // Then:
        verify(docStatusLoader).load(key); // only load the value once
        assertThat(result1, equalTo(docStats));
        assertThat(result2, equalTo(docStats));
    }

    public void testTextFlowTargetHasError() throws Exception {
        // Given:
        Long targetId = new Long("1000");
        ValidationId validationId = ValidationId.HTML_XML;
        Map<ValidationId, Boolean> map = new HashMap<ValidationId, Boolean>();

        // When:
        when(targetValidationLoader.load(targetId)).thenReturn(map);

        // Run:
        Boolean result =
                tsCache.textFlowTargetHasWarningOrError(targetId, validationId);

        // Then:
        verify(targetValidationLoader).load(targetId); // only load the value
                                                       // once
        assertThat(result, equalTo(null));
    }

    public void testTextFlowTargetHasError2() throws Exception {
        // Given:
        Long targetId = new Long("1000");
        ValidationId validationId = ValidationId.HTML_XML;
        Map<ValidationId, Boolean> map = new HashMap<ValidationId, Boolean>();
        map.put(validationId, true);

        // When:
        when(targetValidationLoader.load(targetId)).thenReturn(map);

        // Run:
        Boolean result =
                tsCache.textFlowTargetHasWarningOrError(targetId, validationId);

        // Then:
        verify(targetValidationLoader).load(targetId); // only load the value
                                                       // once
        assertThat(result, equalTo(true));
    }
}
