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
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extend this class if you want to store objects in the cache that
 * will be updated whenever any of the watched resources in a content
 * manager changes (a resource changes when it is added, deleted or
 * modified).
 * @author Adam Warski (adamw@aster.pl)
 */
public abstract class ShotokuResourceWatcher<K, T> extends ShotokuCacheItem<K, T> {
    private final static Logger log = Logger.getLogger(ShotokuResourceWatcher.class);

    private ConcurrentMap<K, ConcurrentMap<String, ResourceStatus>> allStatuses;

    private ConcurrentMap<String, ResourceStatus> getStatuses(K key) {
        ConcurrentMap<String, ResourceStatus> ret = allStatuses.get(key);
        if (ret == null) {
            ret = new ConcurrentHashMap<String, ResourceStatus>();
            allStatuses.put(key, ret);
        }

        return ret;
    }

    public ShotokuResourceWatcher() {
        allStatuses = new ConcurrentHashMap<K,
                ConcurrentMap<String, ResourceStatus>>();
    }

    /**
     * Adds a path to the watched resources.
     * @param key
     * @param path
     */
    protected void addWatchedPath(K key, String path) {
        getStatuses(key).putIfAbsent(path, new ResourceStatus(getContentManager(key), path));
    }

    protected void deleteWatchedPath(K key, String path) {
        getStatuses(key).remove(path);
    }

    protected void resetWatchedPaths(K key) {
        getStatuses(key).clear();
    }

    public ValueChange<T> update(K key, T currentObject) {
        // Checking all paths.
        Map<String, ResourceStatus> statuses = getStatuses(key);
        Map<String, ChangeType> changes = new HashMap<String, ChangeType>();

        ContentManager cm = getContentManager(key);
        if (cm == null) {
            log.warn("Null content manager for key " + key + ".");
            return ValueChange.noChange();
        }
        
        for (String path : statuses.keySet()) {
            ResourceStatus nrs = new ResourceStatus(cm, path);
            ChangeType ct = statuses.get(path).compareTo(nrs);

            if (ct != ChangeType.NONE) {
                changes.put(path, ct);
                statuses.put(path, nrs);
            }
        }

        if (changes.size() != 0) {
            // Changes found, invoking the update function.
            return update(key, currentObject, changes);
        }
        
        return ValueChange.noChange();
    }

    /**
     * Same as ShotokuCacheItem.update(String, T), but is called only when
     * some of the watched resources change, not on each call of update by
     * the service.
     * @param key Key of the object to update.
     * @param currentObject Current value held in the cache.
     * @param changes A map of descripiton of changes that occured since last
     * update.
     * @return Can return either:
     * <ul>
     * <li>{@link ValueChange#noChange()} to indicate that the value currently held in the cache
     * shouldn't be changed.</li>
     * <li>{@link ValueChange#changeTo(Object)} to bind a new value to the given key in
     * the cache.</li>
     * </ul>
     */
    protected abstract ValueChange<T> update(K key, T currentObject,
                                   Map<String, ChangeType> changes);

    private class ResourceStatus {
        private long lastMod;
        private boolean exists;

        public ResourceStatus(ContentManager cm, String path) {
            try {
                lastMod = cm.getNode(path).getLastModification();
                exists = true;
            } catch (ResourceDoesNotExist e1) {
                try {
                    lastMod = cm.getDirectory(path).getLastModification();
                    exists = true;
                } catch (ResourceDoesNotExist e2) {
                    exists = false;
                }
            }
        }

        /**
         * Compares this resource status to the given one, which is
         * assumed to be "newer".
         * @param status Status to which to compare.
         * @return A type of change between the two statuses.
         */
        public ChangeType compareTo(ResourceStatus status) {
            if ((exists) && (!status.exists)) {
                return ChangeType.DELETED;
            }

            if ((!exists) && (status.exists)) {
                return ChangeType.ADDED;
            }

            if (lastMod != status.lastMod) {
                return ChangeType.MODIFIED;
            }

            return ChangeType.NONE;
        }
    }
}
