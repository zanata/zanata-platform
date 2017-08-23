/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.zanata.rest.editor.service.resource;

import org.zanata.rest.editor.MediaTypes;
import org.zanata.webtrans.shared.rest.TransMemoryMergeResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.List;

/**
 * Endpoint to search for suggestions from translation memory and other sources.
 */
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface SuggestionsResource extends
        TransMemoryMergeResource, Serializable {

    String SERVICE_PATH = "/suggestions";

    /**
     * Retrieves a list of suggestions for a a query in the body of the request.
     *
     * POST is used to allow the potentially long query strings to be sent in the
     * body rather than the query string.
     *
     * @param query a JSON array of query strings in the body of the request,
     *              used to look up similar or identical strings based on
     *              sourceLocale, that have been translated to transLocale.
     * @param sourceLocale locale id in the form lang[-country[-modifier]]
     * @param transLocale locale id in the form lang[-country[-modifier]]
     * @param searchType the search type to use, determines how similar source
     *                   strings must be to be considered a match. Valid
     *                   values are "EXACT", "FUZZY", "RAW", "FUZZY_PLURAL"
     *                   and "CONTENT_HASH" as defined in
     *                   {@link org.zanata.webtrans.shared.rpc.HasSearchType.SearchType}.
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK (200) - Response containing a list of suggestions. <br>
     *         BAD REQUEST (400) - If searchType is not a valid search type, or if
     *             sourceLocale or transLocale are malformed or not available
     *             on the server.
     *         INTERNAL SERVER ERROR (500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @POST
    @Produces({ MediaTypes.APPLICATION_ZANATA_SUGGESTIONS_JSON, MediaType.APPLICATION_JSON })
    Response query(List<String> query,
            @QueryParam("from") String sourceLocale,
            @QueryParam("to") String transLocale,
            @QueryParam("searchType") @DefaultValue("FUZZY_PLURAL") String searchType,
            @QueryParam("textFlowTargetId") @DefaultValue("-1") long textFlowTargetId);
}
