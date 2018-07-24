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

import javax.annotation.Nullable;

import com.google.common.cache.CacheLoader;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class InfinispanCacheWrapper<K, V> implements CacheWrapper<K, V> {

    private final Cache<K, V> cache;
    private final @Nullable CacheLoader<K, V> cacheLoader;

    public InfinispanCacheWrapper(Cache<K, V> cache,
            CacheLoader<K, V> cacheLoader) {
        this.cache = cache;
        this.cacheLoader = cacheLoader;
    }

    public InfinispanCacheWrapper(Cache<K, V> cache) {
        this(cache, null);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V get(K key) {
        return cache.get(key);
    }

    @Override
    public synchronized V getWithLoader(K key) {
        // NB: Need to manually implement the cache loader feature
        V cachedValue = cache.get(key);
        if (cachedValue == null && cacheLoader != null) {
            try {
                cachedValue = cacheLoader.load(key);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Unable to load entry with cache loader ", e);
            }
            cache.put(key, cachedValue);
        }
        return cachedValue;
    }

    @Override
    public boolean remove(K key) {
        return cache.remove(key) != null;
    }

    public Cache<K, V> getCache() {
        return cache;
    }

    public static <K, V> InfinispanCacheWrapper<K, V> create(
            final String cacheName,
            final CacheContainer cacheManager) {
        return create(cacheName, cacheManager, null);
    }

    public static <K, V> InfinispanCacheWrapper<K, V> create(
            final String cacheName,
            final CacheContainer cacheManager,
            final CacheLoader<K, V> loader) {
        return create(cacheManager.getCache(cacheName), loader);

    }

    public static <K, V> InfinispanCacheWrapper<K, V> create(
            final Cache<K, V> cache,
            final @Nullable CacheLoader<K, V> loader) {
        return new InfinispanCacheWrapper<>(cache, loader);
    }
}
