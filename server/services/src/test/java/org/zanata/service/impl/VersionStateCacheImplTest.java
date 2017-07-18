package org.zanata.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.infinispan.manager.CacheContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.cache.InfinispanTestCacheContainer;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.service.VersionLocaleKey;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.model.statistic.WordStatistic;

import com.google.common.cache.CacheLoader;
import org.zanata.util.IServiceLocator;
import org.zanata.util.Zanata;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class VersionStateCacheImplTest {

    @Inject
    private VersionStateCacheImpl cache;
    @Produces @Mock
    private CacheLoader<VersionLocaleKey, WordStatistic> versionStatisticLoader;

    @Produces @Mock IServiceLocator serviceLocator;
    @Produces @Mock LocaleDAO localeDAO;

    @Produces
    @Zanata
    CacheContainer cacheContainer = new InfinispanTestCacheContainer();

    @Test
    public void getStatisticTest() throws Exception {

        Long projectIterationId = 1L;
        LocaleId localeId = LocaleId.EN_US;
        VersionLocaleKey key = new VersionLocaleKey(projectIterationId, localeId);

        WordStatistic wordStatistic = new WordStatistic(0, 11, 100, 4, 500);

        // When:
        when(versionStatisticLoader.load(key)).thenReturn(wordStatistic);

        WordStatistic result = cache.getVersionStatistics(projectIterationId,
            localeId);

        // Then:
        verify(versionStatisticLoader).load(key); // only load the value once
        assertThat(result, equalTo(wordStatistic));
    }
}
