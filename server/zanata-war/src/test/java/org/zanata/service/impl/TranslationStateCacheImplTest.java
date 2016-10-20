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

import com.google.common.cache.CacheLoader;
import org.hibernate.Session;
import org.infinispan.manager.CacheContainer;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.cache.InfinispanTestCacheContainer;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.Zanata;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.model.ValidationId;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(CdiUnitRunner.class)
@SupportDeltaspikeCore
public class TranslationStateCacheImplTest {

    @Inject
    TranslationStateCacheImpl tsCache;

    @Produces @Mock
    private CacheLoader<DocumentLocaleKey, WordStatistic> docStatisticLoader;
    @Produces @Mock
    private CacheLoader<DocumentLocaleKey, DocumentStatus> docStatusLoader;
    @Produces @Mock
    private CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader;

    @Produces @Mock Session session;
    @Produces @Mock TextFlowTargetDAO textFlowTargetDAO;
    @Produces @Mock TextFlowDAO textFlowDAO;
    @Produces @Mock LocaleDAO localeDAO;
    @Produces @Mock DocumentDAO documentDAO;

    @Produces
    @Zanata
    CacheContainer cacheContainer = new InfinispanTestCacheContainer();

    @Test
    public void testGetLastModifiedTextFlowTarget() throws Exception {
        // Given:
        Long documentId = new Long("100");
        LocaleId testLocaleId = LocaleId.DE;
        DocumentLocaleKey key =
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

    @Test
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

    @Test
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
