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

import com.google.common.cache.CacheLoader;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class InfinispanCacheWrapper<K, V> implements CacheWrapper<K, V> {

    private final String cacheName;
    private final CacheContainer cacheContainer;
    private CacheLoader<K, V> cacheLoader;

    public InfinispanCacheWrapper(String cacheName,
            CacheContainer cacheContainer) {
        this.cacheName = cacheName;
        this.cacheContainer = cacheContainer;
    }

    public InfinispanCacheWrapper(String cacheName,
            CacheContainer cacheContainer,
            CacheLoader<K, V> cacheLoader) {
        this(cacheName, cacheContainer);
        this.cacheLoader = cacheLoader;
    }

    @Override
    public void put(K key, V value) {
        getCache().put(key, value);
    }

    @Override
    public V get(K key) {
        return getCache().get(key);
    }

    @Override
    public synchronized V getWithLoader(K key) {
        // NB: Need to manually implement the cache loader feature
        V cachedValue = getCache().get(key);
        if(cachedValue == null && cacheLoader != null) {
            try {
                cachedValue = cacheLoader.load(key);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Unable to load entry with cache loader ", e);
            }
            getCache().put(key, cachedValue);
        }
        return cachedValue;
    }

    @Override
    public boolean remove(K key) {
        return getCache().remove(key) != null;
    }

    public Cache<K, V> getCache() {
        return cacheContainer.getCache(cacheName);
    }

    public static <K, V> InfinispanCacheWrapper<K, V> create(
            final String cacheName,
            final CacheContainer cacheManager) {
        cacheManager.getCache(cacheName);
        return new InfinispanCacheWrapper<K, V>(cacheName, cacheManager);
    }

    public static <K, V> InfinispanCacheWrapper<K, V> create(
            final String cacheName,
            final CacheContainer cacheManager, CacheLoader<K, V> loader) {
        cacheManager.getCache(cacheName);
        return new InfinispanCacheWrapper<K, V>(cacheName, cacheManager, loader);
    }
}
