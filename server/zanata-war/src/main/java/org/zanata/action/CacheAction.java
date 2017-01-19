/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.action;

import com.google.common.base.Throwables;
import com.google.common.html.HtmlEscapers;
import org.apache.commons.beanutils.BeanUtils;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.stats.Stats;
import org.zanata.i18n.Messages;
import org.zanata.security.annotations.CheckRole;
import org.zanata.util.Zanata;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Armagan Ersoz
 *         <a href="mailto:aersoz@redhat.com">aersoz@redhat.com</a>
 */
@CheckRole("admin")
@Named("cacheAction")
@ViewScoped
public class CacheAction implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CacheAction.class);
    @Inject
    @Zanata
    private EmbeddedCacheManager cacheManager;
    @Inject
    private Messages msgs;

    public CacheAction() {
    }

    public List<String> getCacheList() {
        ArrayList<String> cacheNames =
                new ArrayList<>(cacheManager.getCacheNames());
        Collections.sort(cacheNames);
        return cacheNames;
    }

    public Stats getStats(String cacheName) {
        log.debug("getting stats for cache \'{}\'", cacheName);
        try {
            Cache<Object, Object> cache = cacheManager.getCache(cacheName);
            if (cache.getCacheConfiguration().jmxStatistics().enabled()) {
                return cache.getAdvancedCache().getStats();
            } else {
                log.debug("Statistics are not enabled for cache \'{}\'",
                        cacheName);
                // -1 just means no stats (same as Infinispan)
                return new UnavailableStats(-1);
            }
            // eg getStats() throws IndexOutOfBoundsException if stats not
            // enabled
        } catch (Exception e) {
            log.warn(
                    "Error getting Stats object - are statistics enabled for cache \'{}\'?",
                    cacheName, e);
            // -2 also means no stats, but serves as a hint that something might
            // be wrong
            return new UnavailableStats(-2);
        }
    }

    /**
     * Return some sanitised HTML for the display name of the cache
     *
     * @param cacheName
     * @return
     */
    public String getDisplayName(String cacheName) {
        String emptyString = msgs.get("jsf.cacheStats.emptyString");
        if (cacheName.isEmpty()) {
            return "<em>(" + emptyString + ")</em>";
        } else {
            // Escape cache name in case it includes user input in future
            return HtmlEscapers.htmlEscaper().escape(cacheName);
        }
    }

    public void clearCache(String cacheName) {
        cacheManager.getCache(cacheName).clear();
    }

    public void resetCacheStats(String cacheName) {
        getStats(cacheName).reset();
    }

    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(this::clearCache);
    }

    /**
     * Return the entire set of properties for which the specified bean provides
     * a read method. In this case, the bean is a stats object. The returning
     * value is the set of StatsImpl (org.infinispan.stats.impl) class's
     * properties. The returning map's keys are plain properties' names (e.g.
     * currentNumberOfEntries) and the map's values are values from the
     * Infinispan Stats object (converted to Strings if necessary). This
     * returning value is used for composing the cache statistics table at the
     * admin site.
     */
    public Map<String, String> getCacheStatsProperties(String cacheName) {
        try {
            Map<String, String> properties =
                    BeanUtils.describe(getStats(cacheName));
            properties.remove("class");
            return properties;
        } catch (IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
            throw Throwables.propagate(e);
        }
    }

    public String getNameOfProperty(String key) {
        return msgs.get("jsf.cacheStats." + key + ".name");
    }

    public String getDescOfProperty(String key) {
        return msgs.get("jsf.cacheStats." + key + ".description");
    }

    /**
     * Dummy Stats implementation returned when Stats are disabled or otherwise
     * unavailable.
     */
    private static class UnavailableStats implements Stats {
        private final int val;

        private UnavailableStats(int val) {
            this.val = val;
        }

        @Override
        public long getTimeSinceStart() {
            return val;
        }

        @Override
        public long getTimeSinceReset() {
            return val;
        }

        @Override
        public int getCurrentNumberOfEntries() {
            return val;
        }

        @Override
        public long getTotalNumberOfEntries() {
            return val;
        }

        @Override
        public long getStores() {
            return val;
        }

        @Override
        public long getRetrievals() {
            return val;
        }

        @Override
        public long getHits() {
            return val;
        }

        @Override
        public long getMisses() {
            return val;
        }

        @Override
        public long getRemoveHits() {
            return val;
        }

        @Override
        public long getRemoveMisses() {
            return val;
        }

        @Override
        public long getEvictions() {
            return val;
        }

        @Override
        public long getAverageReadTime() {
            return val;
        }

        @Override
        public long getAverageWriteTime() {
            return val;
        }

        @Override
        public long getAverageRemoveTime() {
            return val;
        }

        @Override
        public void reset() {
        }

        @Override
        public void setStatisticsEnabled(boolean enabled) {
        }
    }
}
