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
package org.zanata.async;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import org.zanata.action.TranslationMemoryAction;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("asyncTaskHandleManager")
@javax.enterprise.context.ApplicationScoped
public class AsyncTaskHandleManager implements Serializable {
    private static final long serialVersionUID = -3209755141964141830L;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private final Map<String, AsyncTaskHandle<?>> handlesByKey = Maps
            .newConcurrentMap();

    // Cache of recently completed tasks
    private Cache<String, AsyncTaskHandle<?>> finishedTasks = CacheBuilder
            .newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public synchronized <K extends AsyncTaskKey> void registerTaskHandle(AsyncTaskHandle handle,
            K key) {
        AsyncTaskHandle<?> existingHandle =
                handlesByKey.putIfAbsent(key.id(), handle);
        Preconditions.checkArgument(existingHandle == null,
                "Task handle with key " + key + " already exists");
    }

    /**
     * Registers a task handle.
     * @param handle The handle to register.
     * @return An auto generated key id to retrieve the handle later
     */
    public synchronized String registerTaskHandle(AsyncTaskHandle handle) {
        GenericKey genericKey = new GenericKey();
        registerTaskHandle(handle, genericKey);
        return genericKey.id();
    }

    void taskFinished(AsyncTaskHandle taskHandle) {
        synchronized (handlesByKey) {
            // TODO This operation is O(n). Maybe we can do better?
            for (Map.Entry<String, AsyncTaskHandle<?>> entry : handlesByKey
                    .entrySet()) {
                if (entry.getValue().equals(taskHandle)) {
                    handlesByKey.remove(entry.getKey());
                    finishedTasks.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public <K extends AsyncTaskKey> AsyncTaskHandle getHandleByKey(K key) {
        return getHandleByKeyId(key.id());
    }

    @SuppressWarnings("unchecked")
    public <T> AsyncTaskHandle<T> getHandleByKeyId(String keyId) {
        if (handlesByKey.containsKey(keyId)) {
            return (AsyncTaskHandle<T>) handlesByKey.get(keyId);
        }
        return (AsyncTaskHandle<T>) finishedTasks.getIfPresent(keyId);
    }

    public Collection<AsyncTaskHandle> getAllHandles() {
        Collection<AsyncTaskHandle> handles = Lists.newArrayList();
        handles.addAll(handlesByKey.values());
        handles.addAll(finishedTasks.asMap().values());
        return handles;
    }

    public Map<String, AsyncTaskHandle<?>> getAllTasks() {
        ImmutableMap.Builder<String, AsyncTaskHandle<?>> builder = ImmutableMap.builder();
        builder.putAll(handlesByKey);
        builder.putAll(finishedTasks.asMap());
        return builder.build();
    }

    public Map<String, AsyncTaskHandle<?>> getRunningTasks() {
        return ImmutableMap.copyOf(handlesByKey);
    }

    public interface AsyncTaskKey extends Serializable {
        /**
         * When converting multiple fields to form id string, we
         * should use this as separator (URL friendly).
         */
        String SEPARATOR = "-";

        String id();

        /**
         * Helper method to convert list of fields to a String as key id.
         *
         * @param keyName
         *            the name for this key
         * @param fields
         *            key instance field values
         * @return String representation of the key which can be used as id
         */
        static String joinFields(String keyName, String... fields) {
            return keyName + SEPARATOR
                    + Joiner.on(SEPARATOR).useForNull("").join(fields);
        }
    }

    public static class GenericKey implements AsyncTaskKey {
        private static final long serialVersionUID = -8648519833116851231L;
        private final String id;

        GenericKey() {
            id = UUID.randomUUID().toString();
        }

        public GenericKey(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("id", id)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GenericKey that = (GenericKey) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
