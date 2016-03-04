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

import java.util.Optional;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.zanata.dao.AuthorizationCodeDAO;
import org.zanata.model.HAccount;
import org.zanata.security.AuthenticationType;
import org.zanata.security.annotations.AuthType;
import org.zanata.security.oauth.SecurityTokens;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path("/oauth/token")
public class TokenService {

    private static final String INVALID_CLIENT_DESCRIPTION =
            "Client authentication failed (e.g., unknown client, no client authentication included, or unsupported authentication method).";

    @Inject
    private SecurityTokens securityTokens;

    @Inject
    private AuthorizationCodeDAO authorizationCodeDAO;

    @Inject
    @AuthType
    private AuthenticationType authenticationType;


    /**
     * This api will verify the short-lived authorization code and if success
     * will generate access token and refresh token then return them.
     *
     * @param request http request
     * @return generated access token and refresh token
     * @throws OAuthSystemException
     */
    @POST
    @Produces("application/json")
    public Response register(@Context HttpServletRequest request) throws
            OAuthSystemException {

        OAuthTokenRequest oauthRequest = null;

        try {
            oauthRequest = new OAuthTokenRequest(request);

            // check if clientid is valid
            String clientId = oauthRequest.getClientId();
            Optional<HAccount> accountOptional = authorizationCodeDAO.getClientIdAuthorizer(
                    clientId);

            if (!accountOptional.isPresent()) {
                OAuthResponse response =
                        OAuthASResponse.errorResponse(
                                HttpServletResponse.SC_BAD_REQUEST)
                                .setError(
                                        OAuthError.TokenResponse.INVALID_CLIENT)
                                .setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                                .buildJSONMessage();
                return Response.status(response.getResponseStatus())
                        .entity(response.getBody()).build();
            }

            // check if client_secret is valid
            // TODO we don't use client secret. We probably should.
//            if (!AuthorizeAction.CLIENT_SECRET.equals(oauthRequest.getClientSecret())) {
//                OAuthResponse response =
//                        OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
//                                .setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT).setErrorDescription(INVALID_CLIENT_DESCRIPTION)
//                                .buildJSONMessage();
//                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
//            }

            // do checking for different grant types
            // at the moment we only support authorization code grant type
            if (!oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
                    .equals(GrantType.AUTHORIZATION_CODE.toString())) {
                OAuthResponse response = OAuthASResponse
                        .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT)
                        .setErrorDescription("invalid grant type")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus())
                        .entity(response.getBody()).build();
            }


            String authCodeParam = oauthRequest.getParam(OAuth.OAUTH_CODE);
            if (!securityTokens
                    .matchAuthorizationCode(authCodeParam)) {
                OAuthResponse response = OAuthASResponse
                        .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT)
                        .setErrorDescription("invalid authorization code")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus())
                        .entity(response.getBody()).build();
            }

            OAuthToken token = securityTokens.generateAccessAndRefreshTokens(authCodeParam);

            authorizationCodeDAO.persistRefreshToken(accountOptional.get(), clientId, token.getRefreshToken());

            OAuthResponse response = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(token.getAccessToken())
                    .setExpiresIn(token.getExpiresIn()
                            .toString())
                    .setRefreshToken(token.getRefreshToken())
                    .buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .entity(response.getBody()).build();

        } catch (OAuthProblemException e) {
            OAuthResponse res = OAuthASResponse
                    .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .error(e)
                    .buildJSONMessage();
            return Response.status(res.getResponseStatus())
                    .entity(res.getBody()).build();
        }

    }

    /**
     * Refresh an expired access token.
     *
     * reference: https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/intro_understanding_refresh_token_oauth.htm
     *
     * @param request http request
     * @return new OAuthToken containing a re-issued access token
     * @throws OAuthSystemException
     */
    @POST
    @Path("/refresh")
    @Produces("application/json")
    public Response refreshToken(@Context HttpServletRequest request) throws
            OAuthSystemException {

        OAuthTokenRequest oauthRequest = null;

        try {
            oauthRequest = new OAuthTokenRequest(request);

            // check if clientid is valid
            String clientId = oauthRequest.getClientId();
            Optional<HAccount> accountOptional = authorizationCodeDAO.getClientIdAuthorizer(
                    clientId);

            if (!accountOptional.isPresent()) {
                OAuthResponse response =
                        OAuthASResponse.errorResponse(
                                HttpServletResponse.SC_BAD_REQUEST)
                                .setError(
                                        OAuthError.TokenResponse.INVALID_CLIENT)
                                .setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                                .buildJSONMessage();
                return Response.status(response.getResponseStatus())
                        .entity(response.getBody()).build();
            }

            if (!oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
                    .equals(GrantType.REFRESH_TOKEN.toString())) {
                OAuthResponse response = OAuthASResponse
                        .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT)
                        .setErrorDescription("invalid grant type")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus())
                        .entity(response.getBody()).build();
            }


            String refreshTokenParam =
                    oauthRequest.getParam(OAuth.OAUTH_REFRESH_TOKEN);
            // this will need to do a database look up
            Optional<String> usernameOpt = authorizationCodeDAO.getUsernameFromClientIdAndFreshToken(clientId, refreshTokenParam);
            if (!usernameOpt.isPresent()) {
                OAuthResponse response = OAuthASResponse
                        .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT)
                        .setErrorDescription("invalid refresh code")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus())
                        .entity(response.getBody()).build();
            }

            OAuthToken token = securityTokens.reissueAccessToken(clientId, refreshTokenParam);

            OAuthResponse response = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(token.getAccessToken())
                    .setExpiresIn(token.getExpiresIn()
                            .toString())
                    .buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .entity(response.getBody()).build();

        } catch (OAuthProblemException e) {
            OAuthResponse res = OAuthASResponse
                    .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .error(e)
                    .buildJSONMessage();
            return Response.status(res.getResponseStatus())
                    .entity(res.getBody()).build();
        }

    }
}
