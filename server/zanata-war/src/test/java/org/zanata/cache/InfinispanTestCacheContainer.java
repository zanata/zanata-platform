/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.cache;

import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.lifecycle.Lifecycle;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;

/**
 * A cache container to be used in tests. Everything is kept in memory in a real
 * Infinispan cache which can be restarted / reset at will.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class InfinispanTestCacheContainer implements CacheContainer {
    private DefaultCacheManager delegate;

    public InfinispanTestCacheContainer() {
        start();
    }

    @Override
    public void start() {
        stop();
        this.delegate =
                new DefaultCacheManager(getCacheManagerGlobalConfiguration());
        this.delegate.start();
    }

    @Override
    public void stop() {
        if (delegate != null) {
            delegate.stop();
        }
    }

    private GlobalConfiguration getCacheManagerGlobalConfiguration() {
        /*
         * This allows multiple concurrent tests to run. See
         * https://issues.jboss.org/browse/ISPN-2886 for the exception that is
         * thrown when this is not used.
         */
        return new GlobalConfigurationBuilder().globalJmxStatistics()
                .allowDuplicateDomains(true).build();
    }

    public <K extends java.lang.Object, V extends java.lang.Object>
            org.infinispan.Cache<K, V> getCache() {
        return this.delegate.<K, V> getCache();
    }

    public <K extends java.lang.Object, V extends java.lang.Object>
            org.infinispan.Cache<K, V> getCache(final java.lang.String arg0) {
        return this.delegate.<K, V> getCache(arg0);
    }
}
