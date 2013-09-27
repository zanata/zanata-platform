/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.cache;

import java.util.Collection;
import java.util.Map;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;

import com.google.common.cache.CacheLoader;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */

@SuppressWarnings("rawtypes")
public class EhcacheLoader<K, V> implements net.sf.ehcache.loader.CacheLoader {

    private CacheLoader<K, V> loader;

    public EhcacheLoader(CacheLoader<K, V> loader) {
        this.loader = loader;
    }

    @Override
    public V load(Object key) throws CacheException {
        try {
            return loader.load((K) key);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public Map<K, V> loadAll(Collection keys) {
        try {
            return loader.loadAll(keys);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public Object load(Object key, Object argument) {
        return load(key);
    }

    @Override
    public Map<K, V> loadAll(Collection keys, Object argument) {
        return loadAll(keys);
    }

    @Override
    public String getName() {
        return getClass().getName() + ":" + loader.getClass().getName();
    }

    @Override
    public net.sf.ehcache.loader.CacheLoader clone(Ehcache cache)
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public void init() {
    }

    @Override
    public void dispose() throws CacheException {
    }

    @Override
    public Status getStatus() {
        return Status.STATUS_ALIVE;
    }

}
