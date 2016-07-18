/*
 *
 *  * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

package org.zanata.service.impl;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import org.infinispan.manager.CacheContainer;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.zanata.cache.CacheWrapper;
import org.zanata.cache.InfinispanCacheWrapper;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.events.DocStatsEvent;
import org.zanata.model.HLocale;
import org.zanata.service.VersionLocaleKey;
import org.zanata.service.VersionStateCache;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.IServiceLocator;

import com.google.common.cache.CacheLoader;
import org.zanata.util.Zanata;

import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
// TODO look at using @Transactional
public class VersionStateCacheImpl implements VersionStateCache {
    private static final String BASE = VersionStateCacheImpl.class.getName();

    private static final String VERSION_STATISTIC_CACHE_NAME = BASE
            + ".versionStatisticCache";

    private  CacheWrapper<VersionLocaleKey, WordStatistic> versionStatisticCache;
    private final CacheLoader<VersionLocaleKey, WordStatistic> versionStatisticLoader;

    @Zanata
    private CacheContainer cacheContainer;
    private final IServiceLocator serviceLocator;
    private final LocaleDAO localeDAO;

    // constructor for CDI
    public VersionStateCacheImpl() {
        this(null, null, null, null);
    }

    // Constructor for testing
    @Inject
    public VersionStateCacheImpl(
            CacheLoader<VersionLocaleKey, WordStatistic> versionStatisticLoader,
            @Zanata CacheContainer cacheContainer,
            IServiceLocator serviceLocator,
            LocaleDAO localeDAO) {
        this.versionStatisticLoader = versionStatisticLoader;
        this.cacheContainer = cacheContainer;
        this.serviceLocator = serviceLocator;
        this.localeDAO = localeDAO;
    }

    @PostConstruct
    public void create() {
        versionStatisticCache =
                InfinispanCacheWrapper.create(VERSION_STATISTIC_CACHE_NAME,
                        cacheContainer, versionStatisticLoader);
    }

    @Override
    public void docStatsUpdated(
        @Observes(during = TransactionPhase.AFTER_SUCCESS)
        DocStatsEvent event) {
        VersionLocaleKey key =
            new VersionLocaleKey(event.getProjectVersionId(),
                event.getKey().getLocaleId());
        WordStatistic stats = versionStatisticCache.get(key);
        if (stats != null) {
            for (Map.Entry<ContentState, Long> entry : event
                    .getWordDeltasByState().entrySet()) {
                stats.increment(entry.getKey(),
                        Math.toIntExact(entry.getValue()));
            }
            versionStatisticCache.put(key, stats);
        }
    }

    @Override
    public WordStatistic getVersionStatistics(Long projectIterationId,
        LocaleId localeId) {
        return versionStatisticCache.getWithLoader(new VersionLocaleKey(
                projectIterationId, localeId));
    }

    @Override
    public void clearVersionStatsCache(Long versionId) {
        for (HLocale locale : localeDAO.findAll()) {
            VersionLocaleKey key =
                    new VersionLocaleKey(versionId, locale.getLocaleId());
            versionStatisticCache.remove(key);
        }
    }

    public static class VersionStatisticLoader extends
            CacheLoader<VersionLocaleKey, WordStatistic> {

        @Inject
        private ProjectIterationDAO projectIterationDAO;

        @Override
        public WordStatistic load(VersionLocaleKey key) throws Exception {

            WordStatistic wordStatistic =
                projectIterationDAO.getWordStatistics(
                        key.getProjectIterationId(), key.getLocaleId());

            return wordStatistic;
        }
    }
}
