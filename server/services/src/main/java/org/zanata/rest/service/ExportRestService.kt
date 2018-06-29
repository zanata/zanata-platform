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

package org.zanata.rest.service

import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.zanata.seam.security.CurrentUser
import org.zanata.service.GraphQLService
import org.zanata.util.toJson
import javax.ws.rs.GET

/**
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com),
 * Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Path("/export")
class ExportRestService @Inject constructor(
        private val currentUser: CurrentUser,
        private val graphQLService: GraphQLService) {

    @GET
    @Path("/userData")
    fun exportUserData(): Response {
        if (!currentUser.isLoggedIn) {
            return Response.status(Response.Status.UNAUTHORIZED).build()
        }

        val graphqlQuery = """
            {
              account (username: "${currentUser.username}") {
                username
                creationDate
                lastChanged
                enabled
                roles { name }
                person {
                  name
                  email
                  # global language teams
                  languageTeamMemberships {
                    locale { localeId }
                    isCoordinator
                    isReviewer
                    isTranslator
                  }
                  # project language teams
                  projectLocaleMemberships {
                    project {
                      slug
                      name
                      # no projectIterations
                    }
                    locale { localeId }
                    role
                  }
                  # project maintainer/owner or translation maintainer
                  projectMemberships {
                    project {
                      slug
                      name
                      projectIterations { slug }
                    }
                    role
                  }
                }
              }
            }""".trimIndent()
//        println(graphqlQuery)
        val result = graphQLService.query(graphqlQuery)

        if (!result.errors.isEmpty()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(result.errors).build()
        }

        val data = result.toSpecification()["data"]!!
        @Suppress("UNCHECKED_CAST")
        val json = toJson(data as Map<String, Any>, pretty = true)
        return Response.ok(json).build()
    }

}
