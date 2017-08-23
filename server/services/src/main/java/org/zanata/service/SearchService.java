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
package org.zanata.service;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.zanata.rest.dto.ReindexStatus;

import java.io.Serializable;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface SearchService extends Serializable {
    /**
     * Requests the start of a system reindex. NOTE: This is not a stable,
     * supported API. It might change from release to release.
     *
     * @param purgeAll
     *            Purges all indexes.
     * @param indexAll
     *            Reindexes all elements.
     * @param optimizeAll
     *            Optimizes all indexes.
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing the Indexing process' status.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @POST
    @Path("/reindex/start")
    ReindexStatus startReindex(@QueryParam("purge") boolean purgeAll,
            @QueryParam("index") boolean indexAll,
            @QueryParam("optimize") boolean optimizeAll);

    /**
     * Returns the status of a system search reindex operation. NOTE: This is
     * not a stable, supported API. It might change from release to release.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing the Indexing process' status if one
     *         is in progress.<br>
     *         NOT FOUND(404) - If there is no indexing task currently in
     *         progress.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path("/reindex")
    ReindexStatus getReindexStatus();
}
