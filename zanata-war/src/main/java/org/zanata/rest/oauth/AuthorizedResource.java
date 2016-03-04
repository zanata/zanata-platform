/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.rest.oauth;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;
import org.jboss.resteasy.util.GenericType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HProject;
import org.zanata.rest.dto.Account;
import org.zanata.rest.dto.Project;
import org.zanata.rest.service.AccountService;
import org.zanata.rest.service.ProjectService;
import org.zanata.security.oauth.SecurityTokens;
import com.google.common.base.Strings;
import com.googlecode.totallylazy.Either;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
@Path("/oauth/authorized")
@Slf4j
@Transactional
public class AuthorizedResource {

    @Inject
    private SecurityTokens securityTokens;

    @Context
    private HttpServletRequest request;

    @Inject
    private ProjectDAO projectDAO;

    @Inject
    private AccountDAO accountDAO;


    @Path("/projects")
    @Produces("application/json")
    @GET
    public Response maintainedProjects() {
        Either<String, Response> usernameOrResponse = getUsernameOrResponse();
        if (usernameOrResponse.isRight()) {
            return usernameOrResponse.right();
        }
        String username = usernameOrResponse.left();

        HAccount account = accountDAO.getByUsername(username);
        // TODO pahuang this is not doing paging properly and can potentially return large data set
        List<HProject> maintainedProjects = projectDAO
                .getProjectsForMaintainer(account.getPerson(), null, 0, 1000);

        List<Project> projects = maintainedProjects.stream()
                .map(hProject -> ProjectService.toResource(hProject,
                        MediaType.APPLICATION_JSON_TYPE))
                .collect(Collectors.toList());
        Type genericType = new GenericType<List<Project>>() {
        }.getGenericType();
        return Response.ok(new GenericEntity<>(projects, genericType)).build();
    }

    @Path("/myaccount")
    @Produces("application/json")
    @GET
    public Response accountDetail() {
        Either<String, Response> usernameOrResponse = getUsernameOrResponse();
        if (usernameOrResponse.isRight()) {
            return usernameOrResponse.right();
        }
        String username = usernameOrResponse.left();

        HAccount account = accountDAO.getByUsername(username);
        if (Strings.isNullOrEmpty(account.getApiKey())) {
            accountDAO.createApiKey(account);
        }
        Account dto = new Account();
        AccountService.transfer(account, dto);
        return Response.ok(dto).build();
    }

    // TODO use annotation to handle all this security
    private Either<String, Response> getUsernameOrResponse() {
        try {
            String accessToken = getAccessToken();
            Optional<String> usernameOpt = checkAccess(accessToken);
            if (!usernameOpt.isPresent()) {
                if (securityTokens.isTokenExpired(accessToken)) {
                    return Either.right(buildUnauthorizedResponse("access token expired"));
                }
                return Either.right(buildUnauthorizedResponse("invalid access code"));
            }
            String username = usernameOpt.get();
            return Either.left(username);

        } catch (OAuthProblemException e) {
            return Either.right(buildUnauthorizedResponse(e.getMessage()));
        } catch (OAuthSystemException e) {
            return Either.right(buildServerErrorResponse(e.getMessage()));
        }
    }

    private Response buildUnauthorizedResponse(String message) {
        OAuthResponse oauthResponse = null;
        try {
            oauthResponse = OAuthRSResponse
                    .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .buildHeaderMessage();
        } catch (OAuthSystemException e1) {
            return buildServerErrorResponse(message);
        }

        return Response
                .status(oauthResponse.getResponseStatus()).header(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse.getHeader(
                OAuth.HeaderType.WWW_AUTHENTICATE)).build();
    }

    private Response buildServerErrorResponse(String message) {
        return Response.serverError().entity(message).build();
    }

    private Optional<String> checkAccess(String accessToken) {
        return securityTokens.matchAccessToken(accessToken);
    }

    private String getAccessToken() throws OAuthSystemException, OAuthProblemException {
        // Make the OAuth Request out of this request and validate it
        // Specify where you expect OAuth access token (request header, body or query string)
        OAuthAccessResourceRequest oauthRequest = new
                OAuthAccessResourceRequest(request, ParameterStyle.HEADER);
        return oauthRequest.getAccessToken();
    }

}
