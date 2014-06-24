/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTask;
import org.zanata.async.TaskExecutor;
import org.zanata.service.AsyncTaskManagerService;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Default Implementation of an Asynchronous task manager service.
 *
 * This replaces the now deprecated ProcessManagerService.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("asyncTaskManagerServiceImpl")
@Scope(ScopeType.APPLICATION)
@Startup
@Slf4j
public class AsyncTaskManagerServiceImpl implements AsyncTaskManagerService {

    // Map of all active task handles
    private Map<Serializable, AsyncTaskHandle> handlesByKey = Maps.newConcurrentMap();

    // Cache of recently completed tasks
    private Cache<Serializable, AsyncTaskHandle> finishedTasks = CacheBuilder
            .newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private long lastAssignedKey = 1;

    /**
     * Starts a task, using a generated task key
     * @param task
     *            The asynchronous task to run.
     * @param <V>
     * @param <H>
     * @return
     */
    @Override
    public <V, H extends AsyncTaskHandle<V>> String startTask(
            AsyncTask<V, H> task) {
        Long taskKey = generateNextAvailableKey();
        startTask(task, taskKey);
        return taskKey.toString();
    }

    /**
     * Starts a task, using the specified task key
     * @param task
     *            The asynchronous task to run.
     * @param key
     * @param <V>
     * @param <H>
     */
    @Override
    public <V, H extends AsyncTaskHandle<V>> void startTask(
            AsyncTask<V, H> task, final Serializable key) {
        TaskExecutor taskExecutor =
                (TaskExecutor) Component.getInstance(TaskExecutor.class);
        AsyncTaskHandle<V> handle = taskExecutor.startTask(task,
                new Runnable() {
                    @Override
                    public void run() {
                        taskFinished(key);
                    }
                });
        AsyncTaskHandle oldHandle = handlesByKey.put(key, handle);
        if (oldHandle != null) {
            log.error(
                    "Key {} has a duplicate: old handle is {}; new handle is {}",
                    key, oldHandle, handle);
        }
    }

    private void taskFinished(Serializable key) {
        AsyncTaskHandle handle = handlesByKey.remove(key);
        if (handle != null) {
            finishedTasks.put(key, handle);
        } else {
            log.error("unknown task key: {}", key);
        }
    }

    /**
     * Gets the handle for a generated task key
     * @param taskId
     *            The task Id (as returned by
     *            {@link AsyncTaskManagerService#startTask(org.zanata.async.AsyncTask)}
     *            )
     * @return
     */
    @Override
    public AsyncTaskHandle getHandle(String taskId) {
        try {
            Long taskKey = Long.parseLong(taskId);
            return getHandleByKey(taskKey);
        } catch (NumberFormatException e) {
            return null; // Non-number keys are not allowed in this
                         // implementation
        }
    }

    /**
     * Gets the handle for a task which was started with a specified key
     * @param key
     *            The task id as provided to
     *            {@link AsyncTaskManagerService#startTask(org.zanata.async.AsyncTask, java.io.Serializable)}
     * @return
     */
    @Override
    public AsyncTaskHandle getHandleByKey(Serializable key) {
        // NB: check the active tasks before finished tasks, in
        // case the task finishes in between
        AsyncTaskHandle handle = handlesByKey.get(key);
        if (handle == null) {
            handle = finishedTasks.getIfPresent(key);
        }
        return handle;
    }

    @Override
    public Collection<AsyncTaskHandle> getAllHandles() {
        Collection<AsyncTaskHandle> handles = Lists.newArrayList();
        handles.addAll(handlesByKey.values());
        handles.addAll(finishedTasks.asMap().values());
        return handles;
    }

    private synchronized long generateNextAvailableKey() {
        return lastAssignedKey++;
    }

}
