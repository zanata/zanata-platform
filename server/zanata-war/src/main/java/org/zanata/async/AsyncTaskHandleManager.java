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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("asyncTaskHandleManager")
@javax.enterprise.context.ApplicationScoped
public class AsyncTaskHandleManager implements Serializable {

    // TODO refactor the key to be something more useful. e.g. an interface Key with method String id for querying purpose
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private final Map<String, AsyncTaskHandle> handlesByKey = Maps
            .newConcurrentMap();

    // Cache of recently completed tasks
    private Cache<String, AsyncTaskHandle> finishedTasks = CacheBuilder
            .newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public synchronized <K extends AsyncTaskKey<?>> void registerTaskHandle(AsyncTaskHandle handle,
            K key) {
        if (handlesByKey.containsKey(key.id())) {
            throw new RuntimeException("Task handle with key " + key
                    + " already exists");
        }
        handlesByKey.put(key.id(), handle);
    }

    /**
     * Registers a task handle.
     * @param handle The handle to register.
     * @return An auto generated key to retreive the handle later
     */
    public synchronized Serializable registerTaskHandle(AsyncTaskHandle handle) {
        String autoGenKey = UUID.randomUUID().toString();
        AsyncTaskKey<String> autoKey = key -> autoGenKey;
        registerTaskHandle(handle, autoKey);
        return autoGenKey;
    }

    void taskFinished(AsyncTaskHandle taskHandle) {
        synchronized (handlesByKey) {
            // TODO This operation is O(n). Maybe we can do better?
            for (Map.Entry<String, AsyncTaskHandle> entry : handlesByKey
                    .entrySet()) {
                if (entry.getValue().equals(taskHandle)) {
                    handlesByKey.remove(entry.getKey());
                    finishedTasks.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public <K extends AsyncTaskKey<K>> AsyncTaskHandle getHandleByKey(K key) {
        if (handlesByKey.containsKey(key.id())) {
            return handlesByKey.get(key.id());
        }
        return finishedTasks.getIfPresent(key.id());
    }

    public AsyncTaskHandle getHandleByKeyId(String keyId) {
        if (handlesByKey.containsKey(keyId)) {
            return handlesByKey.get(keyId);
        }
        return finishedTasks.getIfPresent(keyId);
    }

    public Collection<AsyncTaskHandle> getAllHandles() {
        Collection<AsyncTaskHandle> handles = Lists.newArrayList();
        handles.addAll(handlesByKey.values());
        handles.addAll(finishedTasks.asMap().values());
        return handles;
    }

    public interface AsyncTaskKey<T> extends Serializable {
        /**
         * When converting multiple fields to form id string or vice versa, we
         * should use this as separator.
         */
        String SEPARATOR = "-";

        default String id() {
            return UUID.randomUUID().toString();
        }

        T from(String id);

        /**
         * Helper method to convert id back to a list of fields.
         *
         * @param id
         *            the id
         * @param keyName
         *            the name for this key
         * @param expectedNumOfFields
         *            number of fields embedded in the id
         * @return list of fields
         */
        default List<String> parseId(String id, String keyName,
                int expectedNumOfFields) {
            String fields = id.replaceFirst(keyName + SEPARATOR, id);
            List<String> result =
                    Splitter.on(SEPARATOR).trimResults().splitToList(fields);
            Preconditions.checkArgument(expectedNumOfFields == result.size(),
                    "%s for %s is invalid", id, keyName);
            return result;
        }

        /**
         * Helper method to convert list of fields to a String as key id.
         *
         * @param keyName
         *            the name for this key
         * @param fields
         *            key instance field values
         * @return String representation of the key which can be used as id
         */
        default String joinFields(String keyName, String... fields) {
            return keyName + SEPARATOR
                    + Joiner.on(SEPARATOR).useForNull("").join(fields);
        }
    }
}
