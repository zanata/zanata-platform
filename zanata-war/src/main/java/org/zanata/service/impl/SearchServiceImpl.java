/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.action.ReindexAsyncBean;
import org.zanata.exception.ZanataServiceException;
import org.zanata.rest.dto.ReindexStatus;
import org.zanata.service.SearchService;

/**
 * Default implementation of a service to manage all Search related activities.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see org.zanata.action.ReindexActionBean
 */
@Name("searchServiceImpl")
@Path("/search")
@Restrict("#{s:hasRole('admin')}")
public class SearchServiceImpl implements SearchService {
    @In
    private ReindexAsyncBean reindexAsync;

    @Override
    public ReindexStatus startReindex(@QueryParam("purge") boolean purgeAll,
            @QueryParam("index") boolean indexAll,
            @QueryParam("optimize") boolean optimizeAll) {
        reindexAsync.setOptions(purgeAll, indexAll, optimizeAll);
        boolean startedReindex = false;

        if (reindexAsync.getProcessHandle().isDone()) {
            startedReindex = true;
            reindexAsync.startProcess();
        }

        ReindexStatus status = this.getReindexStatus();
        status.setStartedReindex(startedReindex);
        return status;
    }

    @Override
    public ReindexStatus getReindexStatus() {
        if (reindexAsync.getProcessHandle().isDone()) {
            throw new ZanataServiceException(
                    "Reindexing not in currently in progress", 404);
        }

        ReindexStatus status = new ReindexStatus();
        status.setCurrentElementType(reindexAsync.getCurrentClassName());
        status.setIndexedElements(reindexAsync.getProcessHandle()
                .getCurrentProgress());
        status.setTotalElements(reindexAsync.getProcessHandle()
                .getMaxProgress());
        // TODO This service is currently not being used
        // To re-add these properties, just implement TimedAsyncHandle
        // status.setTimeElapsed(reindexAsync.getProcessHandle().getElapsedTime());
        // status.setTimeRemaining(reindexAsync.getProcessHandle().getEstimatedTimeRemaining());

        return status;
    }
}
