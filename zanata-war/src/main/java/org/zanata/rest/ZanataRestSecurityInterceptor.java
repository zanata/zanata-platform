package org.zanata.rest;

import java.io.IOException;
import java.util.Optional;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;
import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.zanata.model.HAccount;
import org.zanata.security.SecurityFunctions;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.oauth.SecurityTokens;
import org.zanata.util.HttpUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.googlecode.totallylazy.Either;
import lombok.extern.slf4j.Slf4j;

@Provider
@PreMatching
@SecurityPrecedence
@Slf4j
public class ZanataRestSecurityInterceptor implements ContainerRequestFilter {
    @Context
    private HttpServletRequest request;

    @Inject
    private SecurityTokens securityTokens;

    @Inject
    private ZanataIdentity zanataIdentity;

    @Inject
    @Authenticated
    private HAccount authenticatedAccount;

    @SuppressWarnings("unused")
    public ZanataRestSecurityInterceptor() {
    }

    @VisibleForTesting
    protected ZanataRestSecurityInterceptor(HttpServletRequest request,
            SecurityTokens securityTokens, ZanataIdentity zanataIdentity,
            HAccount authenticatedAccount) {
        this.request = request;
        this.securityTokens = securityTokens;
        this.zanataIdentity = zanataIdentity;
        this.authenticatedAccount = authenticatedAccount;
    }

    @Override
    public void filter(ContainerRequestContext context)
            throws IOException {
        if (authenticatedAccount != null) {
            // request come from the same browser and the user has logged in
            return;
        }

        String username = HttpUtil.getUsername(context.getHeaders());
        String apiKey = HttpUtil.getApiKey(context.getHeaders());

        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(apiKey)) {
            // if apiKey presents, we use apiKey for security check
            zanataIdentity.getCredentials().setUsername(username);
            zanataIdentity.setApiKey(apiKey);
        } else {
            Either<String, Response> usernameOrResponse =
                getUsernameOrResponse();
            if (usernameOrResponse.isRight()) {
                if (SecurityFunctions
                        .canAccessRestPath(zanataIdentity, context.getMethod(),
                                context.getUriInfo().getPath())) {
                    return;
                }
                context.abortWith(usernameOrResponse.right());
                return;
            }
            username = usernameOrResponse.left();
            zanataIdentity.getCredentials().setUsername(username);
            zanataIdentity.setOAuthRequest(true);
        }

        zanataIdentity.tryLogin();
        if (!SecurityFunctions.canAccessRestPath(zanataIdentity,
                context.getMethod(), context.getUriInfo().getPath())) {
            log.info(InvalidApiKeyUtil.getMessage(username, apiKey));
            context.abortWith(Response.status(Status.UNAUTHORIZED)
                    .entity("Invalid token")
                    .build());
        }
    }

    private String getAccessToken() throws OAuthSystemException, OAuthProblemException {
        OAuthAccessResourceRequest oauthRequest = new
                OAuthAccessResourceRequest(request, ParameterStyle.HEADER);
        return oauthRequest.getAccessToken();
    }

    private Optional<String> checkAccess(String accessToken) {
        if (Strings.isNullOrEmpty(accessToken)) {
            return Optional.empty();
        }
        return securityTokens.matchAccessToken(accessToken);
    }

    private Either<String, Response> getUsernameOrResponse() {

        try {
            String authorizationCode = request.getParameter(OAuth.OAUTH_CODE);

            Optional<String> usernameOpt;
            String accessToken = null;
            if (StringUtils.isNotEmpty(authorizationCode)) {
                usernameOpt =
                        securityTokens.findUsernameForAuthorizationCode(authorizationCode);
            } else {
                accessToken = getAccessToken();
                usernameOpt = checkAccess(accessToken);
            }

            if (!usernameOpt.isPresent()) {
                if (!Strings.isNullOrEmpty(accessToken) && securityTokens.isTokenExpired(accessToken)) {
                    return Either.right(buildUnauthorizedResponse("access token expired"));
                }
                return Either.right(buildUnauthorizedResponse("failed authorization"));
            }
            String username = usernameOpt.get();
            return Either.left(username);

        } catch (OAuthProblemException e) {
            log.warn("OAuth problem: {}", e.getMessage());
            return Either.right(buildUnauthorizedResponse(e.getMessage()));
        } catch (OAuthSystemException e) {
            log.error("OAuth system exception", e);
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
}
