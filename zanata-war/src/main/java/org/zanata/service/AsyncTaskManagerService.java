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
package org.zanata.service;

import java.io.Serializable;
import java.util.Collection;

import org.zanata.async.AsyncTask;
import org.zanata.async.AsyncTaskHandle;

/**
 * The Task manager Service offers a central location where tasks running
 * asynchronously can be started, and queried.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface AsyncTaskManagerService {
    /**
     * Starts a task asynchronously.
     *
     * @param task
     *            The asynchronous task to run.
     * @return The key by which the handle for the task may be retrieved.
     */
    <V, H extends AsyncTaskHandle<V>> String startTask(AsyncTask<V, H> task);

    /**
     * Starts a task asynchronously while providing a key to store the task's
     * handle. The handle may be retrieved later using the provided key.
     *
     * @param task
     *            The asynchronous task to run.
     * @param key
     *            The key that may be used to retrieve the task handle later.
     */
    <V, H extends AsyncTaskHandle<V>> void startTask(AsyncTask<V, H> task,
            Serializable key);

    /**
     * Retrieves a task handle.
     *
     * @param taskId
     *            The task Id (as returned by
     *            {@link AsyncTaskManagerService#startTask(org.zanata.async.AsyncTask)}
     *            )
     * @return A task handle, or null if the task with said Id is not being
     *         handled by this service.
     */
    AsyncTaskHandle getHandle(String taskId);

    /**
     * Retrieves a task handle.
     *
     * @param key
     *            The task id as provided to
     *            {@link AsyncTaskManagerService#startTask(org.zanata.async.AsyncTask, java.io.Serializable)}
     * @return A task handle, or null if there is no task with the provided id
     *         being handled by this service.
     */
    AsyncTaskHandle getHandleByKey(Serializable key);

    /**
     * @return All handles for all tasks being managed by this service.
     */
    Collection<AsyncTaskHandle> getAllHandles();

    /**
     * Clears all the inactive handles managed by this service. Inactive handles
     * all all those handles that reference a task that is either finished, has
     * encountered an error, or has been cancelled.
     */
    void clearInactive();
}
