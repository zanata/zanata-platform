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
package org.zanata.service;

import org.zanata.action.ReindexClassOptions;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface IndexingService extends Serializable {
    @Async
    Future<Void> startIndexing(
            Map<Class<?>, ReindexClassOptions> indexingOptions,
            AsyncTaskHandle<Void> handle)
            throws Exception;

    /**
     * This will re-index all HTextFlowTargets under a given project.
     */
    @Async
    Future<Void> reindexHTextFlowTargetsForProject(HProject hProject,
            AsyncTaskHandle<Void> handle)
            throws Exception;

    /**
     * This will re-index all HTextFlowTargets under a given project version.
     * @param iteration project version
     * @param handle async handle
     */
    @Async
    Future<Void> reindexHTextFlowTargetsForProjectIteration(HProjectIteration iteration,
            AsyncTaskHandle<Void> handle);
}
