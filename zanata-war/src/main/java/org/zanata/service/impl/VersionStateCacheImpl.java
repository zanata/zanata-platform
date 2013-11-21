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

import net.sf.ehcache.CacheManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.zanata.cache.CacheWrapper;
import org.zanata.cache.EhcacheWrapper;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.service.VersionLocaleKey;
import org.zanata.service.VersionStateCache;
import org.zanata.ui.model.statistic.WordStatistic;

import com.google.common.cache.CacheLoader;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("versionStateCacheImpl")
@Scope(ScopeType.APPLICATION)
public class VersionStateCacheImpl implements VersionStateCache {
    private static final String BASE = VersionStateCacheImpl.class.getName();

    private static final String VERSION_STATISTIC_CACHE_NAME = BASE
            + ".versionStatisticCache";

    private CacheManager cacheManager;

    private CacheWrapper<VersionLocaleKey, WordStatistic> versionStatisticCache;
    private CacheLoader<VersionLocaleKey, WordStatistic> versionStatisticLoader;

    // constructor for Seam
    public VersionStateCacheImpl() {
        this(new VersionStatisticLoader());
    }

    // Constructor for testing
    public VersionStateCacheImpl(
            CacheLoader<VersionLocaleKey, WordStatistic> versionStatisticLoader) {
        this.versionStatisticLoader = versionStatisticLoader;
    }

    @Create
    public void create() {
        cacheManager = CacheManager.create();
        versionStatisticCache =
                EhcacheWrapper.create(VERSION_STATISTIC_CACHE_NAME,
                        cacheManager, versionStatisticLoader);
    }

    @Destroy
    public void destroy() {
        cacheManager.shutdown();
    }

    @Observer(TextFlowTargetStateEvent.EVENT_NAME)
    @Override
    public void textFlowStateUpdated(TextFlowTargetStateEvent event) {
        VersionLocaleKey key =
                new VersionLocaleKey(event.getProjectIterationId(),
                        event.getLocaleId());
        WordStatistic stats = versionStatisticCache.get(key);

        if (stats != null) {
            stats.decrement(event.getPreviousState(), 1);
            stats.increment(event.getNewState(), 1);
            versionStatisticCache.put(key, stats);
        }
    }

    @Override
    public WordStatistic getVersionStatistics(Long projectIterationId,
        LocaleId localeId) {
        return versionStatisticCache.getWithLoader(new VersionLocaleKey(
                projectIterationId, localeId));
    }

    private static class VersionStatisticLoader extends
            CacheLoader<VersionLocaleKey, WordStatistic> {

        ProjectIterationDAO getProjectIterationDAO() {
            return (ProjectIterationDAO) Component
                    .getInstance(ProjectIterationDAO.class);
        }

        @Override
        public WordStatistic load(VersionLocaleKey key) throws Exception {

            WordStatistic wordStatistic =
                    getProjectIterationDAO().getWordStatistics(
                        key.getProjectIterationId(), key.getLocaleId());

            return wordStatistic;
        }
    }
}
