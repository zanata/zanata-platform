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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import javax.inject.Named;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("asyncTaskHandleManager")
@javax.enterprise.context.ApplicationScoped

public class AsyncTaskHandleManager {

    private Map<Serializable, AsyncTaskHandle> handlesByKey = Maps
            .newConcurrentMap();

    // Cache of recently completed tasks
    private Cache<Serializable, AsyncTaskHandle> finishedTasks = CacheBuilder
            .newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public synchronized void registerTaskHandle(AsyncTaskHandle handle,
            Serializable key) {
        if (handlesByKey.containsKey(key)) {
            throw new RuntimeException("Task handle with key " + key
                    + " already exists");
        }
        handlesByKey.put(key, handle);
    }

    /**
     * Registers a task handle.
     * @param handle The handle to register.
     * @return An auto generated key to retreive the handle later
     */
    public synchronized Serializable registerTaskHandle(AsyncTaskHandle handle) {
        Serializable autoGenKey = UUID.randomUUID().toString();
        registerTaskHandle(handle, autoGenKey);
        return autoGenKey;
    }

    void taskFinished(AsyncTaskHandle taskHandle) {
        synchronized (handlesByKey) {
            // TODO This operation is O(n). Maybe we can do better?
            for (Map.Entry<Serializable, AsyncTaskHandle> entry : handlesByKey
                    .entrySet()) {
                if (entry.getValue().equals(taskHandle)) {
                    handlesByKey.remove(entry.getKey());
                    finishedTasks.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public AsyncTaskHandle getHandleByKey(Serializable key) {
        if (handlesByKey.containsKey(key)) {
            return handlesByKey.get(key);
        }
        return finishedTasks.getIfPresent(key);
    }

    public Collection<AsyncTaskHandle> getAllHandles() {
        Collection<AsyncTaskHandle> handles = Lists.newArrayList();
        handles.addAll(handlesByKey.values());
        handles.addAll(finishedTasks.asMap().values());
        return handles;
    }
}
