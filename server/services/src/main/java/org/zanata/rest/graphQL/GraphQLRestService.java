/*
 * Copyright 2018, Red Hat, Inc. and individual contributors as indicated by the
 *  @author tags. See the copyright.txt file in the distribution for a full
 *  listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */

package org.zanata.rest.graphQL;

import com.google.common.collect.ImmutableList;
import graphql.ExecutionResult;
import org.zanata.seam.security.CurrentUser;
import org.zanata.service.GraphQLService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
@RequestScoped
@Named("GraphQLRestService")
@Path("graphql")
public class GraphQLRestService {
    private CurrentUser currentUser;
    private GraphQLService graphQLService;
//    Projects, versions (id, name)
//    Global and project language teams
//    Review comments (optional)

    @Inject
    public GraphQLRestService(CurrentUser currentUser,
        GraphQLService graphQLService) {
        this.currentUser = currentUser;
        this.graphQLService = graphQLService;
    }

    @Path("/user")
    public Response exportUserData() {
        if (!currentUser.isLoggedIn()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        StringBuilder queryBuilder = new StringBuilder()
            .append("{ user (username: \"")
            .append(currentUser.getUsername())
            .append("\") {")
            .append("username, ")
            .append("apiKey ")
            .append("person { ")
            .append("name ")
            .append("email ")
//            .append(" languageTeamMemberships { ")
//            .append("role ")
            .append(" }}}");

        ExecutionResult result = graphQLService.query(queryBuilder.toString());
        if (!result.getErrors().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(result.getErrors()).build();
        }
        return Response.ok(result.getData().toString()).build();
    }
}
