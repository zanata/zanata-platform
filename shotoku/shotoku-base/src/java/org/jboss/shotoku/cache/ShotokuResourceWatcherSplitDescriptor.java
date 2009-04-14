package org.jboss.shotoku.cache;

import org.jboss.shotoku.exceptions.RepositoryException;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Same as ShotokuResourceWatcher but the update method has a different
 * signature (the information about changes to resources is split into
 * three sets).
 * @author Adam Warski (adamw@aster.pl)
 */
public abstract class ShotokuResourceWatcherSplitDescriptor<K, T>
        extends ShotokuResourceWatcher<K, T> {
    /**
     * Same as ShotokuCacheItem.update(String, T), but is called only when
     * some of the watched resources change, not on each call of update by
     * the service. If the object in the cache should be changed, the
     * implementing method must call put(key, newObject).
     * @param key Key of the object to update.
     * @param currentObject Current value held in the cache.
     * @param modified Set of paths that changed since the last update.
     * @param added Set of paths that were added since the last update.
     * @param deleted Set of paths that were deleted the last update.
     */
    abstract protected ValueChange<T> update(K key, T currentObject, Set<String> modified,
                                   Set<String> added, Set<String> deleted);

    protected ValueChange<T> update(K key, T currentObject, Map<String, ChangeType> changes) {
        Set<String> modified = new HashSet<String>();
        Set<String> added = new HashSet<String>();
        Set<String> deleted = new HashSet<String>();

        for (String path : changes.keySet()) {
            switch (changes.get(path)) {
                case ADDED:
                    added.add(path);
                    break;
                case DELETED:
                    deleted.add(path);
                    break;
                case MODIFIED:
                    modified.add(path);
                    break;
                default:
                    throw new RepositoryException("Invalid change type!");
            }
        }

        return update(key, currentObject, modified, added, deleted);
    }
}
