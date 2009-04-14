/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.shotoku.cache;

import org.jboss.shotoku.ContentManager;

import java.util.*;

/**
 * Extend this class if you want to store objects in the cache that
 * will be updated on every Shotoku service timer timeout. The cache
 * item will be auto-registered in the service upon construction - so
 * take care when constructing objects of this class.
 * @param <K> Type of the key of the objects held in cache. The keys
 * should bahave well as map keys (most probably, the hashCode() and
 * equals() methods should be overriden).
 * @param <T> Type of the object that will be stored in the cache.
 * @author Adam Warski (adamw@aster.pl)
 */
public abstract class ShotokuCacheItem<K, T> implements CacheItemDataSource<K, T> {
    private final Map<K, ContentManager> cmForKeys = new HashMap<K, ContentManager>();

    /**
     * Use this to bind a content manager with a key. There will always be
     * at most one content manager for each key. On first call for a given
     * key, initContentManager(key) will be called.
     * @param key Key for which to get the content manager.
     * @return Content manager bound with the given key.
     */
    protected final ContentManager getContentManager(K key) {
        synchronized (cmForKeys) {
            ContentManager ret = cmForKeys.get(key);

            if (ret == null) {
                ret = initContentManager(key);
                cmForKeys.put(key, ret);
            }

            return ret;
        }
    }

    /**
     * Called when getContentManager(key) is called for the first time.
     * By default returns the default content manager with no prefix.
     * @param key Key for which the content manager should be initialized.
     * @return Default content manager with no prefix.
     */
    protected ContentManager initContentManager(K key) {
        return ContentManager.getContentManager();
    }
    
    public String getInfo() {
    	return null;
    }
}
